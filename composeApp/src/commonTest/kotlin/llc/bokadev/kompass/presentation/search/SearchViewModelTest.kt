package llc.bokadev.kompass.presentation.search

import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.testutil.FakeNavigator
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.RecentSearchesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val index = listOf(
        SearchIndexItem(SearchKind.PLACE, "pima", "Pima Palace", area = "Old Town"),
        SearchIndexItem(SearchKind.PLACE, "lovcen", "Lovćen & the Njegoš Mausoleum", area = "Lovćen"),
        SearchIndexItem(SearchKind.PLACE, "bujovic", "Bujović Palace", area = "Perast"),
        SearchIndexItem(SearchKind.STORY, "spirit", "The spirit of Pima", area = "Old Town"),
        SearchIndexItem(SearchKind.ESSENTIAL, "bus", "Main bus station", essentialCategory = EssentialCategory.TRANSPORT),
        SearchIndexItem(SearchKind.ACTIVITY, "kayak", "Sunset kayak", area = "Dobrota", aliases = listOf("Sea kayak tour"))
    )

    // --- Matching (pure) ---

    @Test fun foldingStripsDiacriticsAndMapsHighlightsBackToTheOriginal() {
        val hit = searchIndex("lovcen", index).single()
        assertEquals("lovcen", hit.item.id)
        assertEquals("Lovćen", hit.item.name.substring(hit.start, hit.start + hit.length))
        assertEquals("bujovic", searchIndex("BUJOVIC", index).single().item.id)
        val njegos = searchIndex("njegos", index).single()
        assertEquals("Njegoš", njegos.item.name.substring(njegos.start, njegos.start + njegos.length))
    }

    @Test fun rankingIsPrefixThenWordPrefixThenSubstringThenAliasThenArea() {
        val hits = searchIndex("pi", index)
        // "Pima Palace" (prefix) before "The spirit of Pima" (word prefix "Pima"): substring "spirit" is not used first.
        assertEquals(listOf("pima", "spirit"), hits.map { it.item.id })
        assertEquals(0 to 2, hits[0].start to hits[0].length)
        assertEquals(1, hits[1].score)
        assertEquals("Pi", hits[1].item.name.substring(hits[1].start, hits[1].start + 2))

        assertEquals(2, searchIndex("ace", index).first().score)
        val alias = searchIndex("tour", index).single()
        assertEquals(3, alias.score); assertEquals(-1, alias.start)
        val area = searchIndex("perast", index).single()
        assertEquals(6, area.score); assertEquals(-1, area.start)
    }

    @Test fun tooShortOrUnknownQueriesFindNothing() {
        assertTrue(searchIndex("p", index).isEmpty())
        assertTrue(searchIndex("  ", index).isEmpty())
        assertTrue(searchIndex("xyz", index).isEmpty())
    }

    @Test fun whitespaceCollapsesOnBothSides() {
        assertEquals("bus", searchIndex("bus   station", index).single().item.id)
        assertEquals("main bus station", foldForSearch("  Main   bus\tstation ").text)
    }

    // --- ViewModel ---

    @Test fun typingDebouncesThenShowsOnlyTypesWithResults() = vmTest { vm, _ ->
        vm.onEvent(SearchEvent.OnQueryChange("bus"))
        advanceTimeBy(100); runCurrent()
        assertFalse(vm.viewStateFlow.value.isSearching)
        advanceTimeBy(60); runCurrent()
        val state = vm.viewStateFlow.value
        assertEquals(listOf(SearchKind.ESSENTIAL to 1), state.kindCounts)
        assertEquals("bus", state.visible.single().item.id)
    }

    @Test fun aNewQueryResetsTheChipToAll() = vmTest { vm, _ ->
        vm.onEvent(SearchEvent.OnQuerySubmit("pi"))
        vm.onEvent(SearchEvent.OnKindChange(SearchKind.STORY))
        assertEquals(listOf("spirit"), vm.viewStateFlow.value.visible.map { it.item.id })
        vm.onEvent(SearchEvent.OnQuerySubmit("pim"))
        assertNull(vm.viewStateFlow.value.selectedKind)
        assertEquals(2, vm.viewStateFlow.value.visible.size)
    }

    @Test fun openingAResultAddsTheQueryToRecentAndNavigates() = vmTest { vm, recent ->
        val events = mutableListOf<SearchEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.eventFlow.collect { events += it } }
        vm.onEvent(SearchEvent.OnQuerySubmit("lovcen"))
        val hit = vm.viewStateFlow.value.visible.single()
        vm.onEvent(SearchEvent.OnResultClick(hit))
        runCurrent()
        assertEquals(listOf("lovcen"), recent.get())
        assertEquals(listOf("lovcen"), vm.viewStateFlow.value.recent)
        assertEquals(listOf<SearchEvent>(SearchEvent.HideKeyboard), events)
        assertEquals(listOf(routeFor(hit.item)), lastNavigator.destinations)
        job.cancel()
    }

    @Test fun browseExploreSwitchesTab() = vmTest { vm, _ ->
        vm.onEvent(SearchEvent.OnBrowseExploreClick)
        runCurrent()
        assertEquals(NavType.SwitchTab(BottomTab.Explore), lastNavigator.recorded.single())
    }

    @Test fun indexFailureIsNotReadyNeverNoResults() = vmTest(fail = true) { vm, _ ->
        vm.onEvent(SearchEvent.OnQuerySubmit("pima"))
        assertEquals(SearchIndexStatus.NOT_READY, vm.viewStateFlow.value.indexStatus)
    }

    @Test fun partialIndexIsReadyButFlaggedIncomplete() = vmTest(failed = setOf(SearchSource.EVENTS)) { vm, _ ->
        vm.onEvent(SearchEvent.OnQuerySubmit("pima"))
        assertEquals(SearchIndexStatus.READY, vm.viewStateFlow.value.indexStatus)
        assertTrue(vm.viewStateFlow.value.isIncomplete)
        assertEquals(2, vm.viewStateFlow.value.hits.size)
    }

    @Test fun retryAfterAFullFailureKeepsTheOldIndexAndClearsIncompleteWhenComplete() = vmTest(failed = setOf(SearchSource.EVENTS)) { vm, _ ->
        vm.onEvent(SearchEvent.OnQuerySubmit("pima"))
        failedSources = emptySet()
        vm.onEvent(SearchEvent.OnRetryClick); runCurrent()
        assertFalse(vm.viewStateFlow.value.isIncomplete)
        fail = true
        vm.onEvent(SearchEvent.OnRetryClick); runCurrent()
        assertEquals(SearchIndexStatus.READY, vm.viewStateFlow.value.indexStatus, "a later failure keeps the index already built")
        assertEquals(2, vm.viewStateFlow.value.hits.size)
    }

    @Test fun aLanguageChangeRebuildsTheIndexAndRerunsTheQuery() = vmTest { vm, _ ->
        vm.onEvent(SearchEvent.OnQuerySubmit("pima"))
        assertEquals(listOf("en"), builtLanguages)
        prefs.setSelectedLanguage("fr"); runCurrent()
        assertEquals(listOf("en", "fr"), builtLanguages)
        assertEquals("pima", vm.viewStateFlow.value.searchedQuery)
        assertEquals(2, vm.viewStateFlow.value.hits.size)
    }

    @Test fun recentKeepsFiveMostRecentFirstWithoutDuplicates() {
        val recent = FakeRecent()
        listOf("a1", "b2", "c3", "d4", "e5", "f6", "B2").forEach(recent::add)
        assertEquals(listOf("B2", "f6", "e5", "d4", "c3"), recent.get())
    }

    private lateinit var lastNavigator: FakeNavigator
    private var fail = false
    private var failedSources = emptySet<SearchSource>()
    private val builtLanguages = mutableListOf<String>()
    private val prefs = Prefs()

    private fun vmTest(fail: Boolean = false, failed: Set<SearchSource> = emptySet(), block: suspend TestScope.(SearchViewModel, FakeRecent) -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        this@SearchViewModelTest.fail = fail
        failedSources = failed
        val recent = FakeRecent()
        val vm = SearchViewModel(
            buildIndex = { lang ->
                builtLanguages += lang
                if (this@SearchViewModelTest.fail) Resource.Error(NetworkError.UNKNOWN, "offline") else Resource.Success(SearchIndex(index, failedSources = failedSources))
            },
            recentSearches = recent,
            preferences = prefs,
            savedState = SavedStateHandle(),
            navigator = FakeNavigator().also { lastNavigator = it }
        )
        try {
            // The index builds when the screen first collects its state.
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.searchState.collect {} }
            runCurrent()
            block(vm, recent)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    /** Same rules as RecentSearchesStore, in memory. */
    private class FakeRecent : RecentSearchesRepository {
        private var items = listOf<String>()
        override fun get() = items
        override fun add(query: String) {
            val q = query.trim().takeIf { it.isNotBlank() } ?: return
            items = (listOf(q) + items.filterNot { it.equals(q, ignoreCase = true) }).take(5)
        }
        override fun clear() { items = emptyList() }
    }

    private class Prefs : AppPreferences {
        private val values = mutableMapOf<String, String>()
        override val selectedLanguageFlow = MutableStateFlow("en")
        override fun getString(key: String) = values[key]
        override fun setString(key: String, value: String?) { if (value == null) values.remove(key) else values[key] = value }
        override fun getSelectedLanguage() = selectedLanguageFlow.value
        override fun setSelectedLanguage(language: String) { selectedLanguageFlow.value = language }
        override fun getAnonymousUserId(): String? = null
        override fun setAnonymousUserId(value: String) = Unit
        override fun isFirstLaunch() = false
        override fun setFirstLaunch(value: Boolean) = Unit
        override fun hasSeenLocationEducationPrompt() = true
        override fun setSeenLocationEducationPrompt(value: Boolean) = Unit
        override fun hasAudioPass() = false
        override fun setAudioPass(value: Boolean) = Unit
        override fun hasExplorerPass() = false
        override fun setExplorerPass(value: Boolean) = Unit
        override fun hasPerksPass() = false
        override fun setPerksPass(value: Boolean) = Unit
    }
}
