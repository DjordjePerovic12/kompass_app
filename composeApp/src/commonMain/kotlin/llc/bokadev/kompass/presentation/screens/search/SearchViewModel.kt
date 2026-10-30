package llc.bokadev.kompass.presentation.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.ALL_CATEGORIES
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.domain.model.SEARCH_LIMIT
import llc.bokadev.kompass.domain.model.SEARCH_MIN_CHARS
import llc.bokadev.kompass.domain.model.SearchHit
import llc.bokadev.kompass.domain.model.SearchIndex
import llc.bokadev.kompass.domain.model.SearchIndexItem
import llc.bokadev.kompass.domain.model.SearchKind
import llc.bokadev.kompass.domain.model.searchIndex
import llc.bokadev.kompass.domain.repository.RecentSearchesRepository

/**
 * Local, destination-only search (Search spec): an in-memory index of cached content, rebuilt
 * when the app language changes. No server call per keystroke; works offline once content exists.
 */
class SearchViewModel(
    /** [llc.bokadev.kompass.domain.usecase.BuildSearchIndexUseCase] in the app; a lambda so tests can hand in an index directly. */
    private val buildIndex: suspend (language: String) -> Result<SearchIndex>,
    private val recentSearches: RecentSearchesRepository,
    private val preferences: AppPreferences,
    private val savedState: SavedStateHandle,
    private val navigator: Navigator
) : BaseViewModel<SearchState, SearchAction, SearchEvent>(
    SearchState(
        query = savedState.get<String>("query").orEmpty(),
        selectedKind = savedState.get<String>("kind")?.let { k -> SearchKind.entries.firstOrNull { it.name == k } },
        recent = recentSearches.get()
    )
) {

    private var index: SearchIndex = SearchIndex(emptyList())
    private var loadJob: Job? = null
    private var debounceJob: Job? = null
    private var language: String = preferences.getSelectedLanguage()

    init {
        viewModelScope.launch {
            preferences.selectedLanguageFlow.collect { lang ->
                // A new language rebuilds the index: names, categories and vocabulary are all localized.
                language = lang
                loadJob?.cancel()
                loadJob = launch { load() }
            }
        }
    }

    override fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.QueryChanged -> {
                setQuery(action.value)
                debounceJob?.cancel()
                debounceJob = viewModelScope.launch {
                    delay(DEBOUNCE_MS)
                    runSearch(keepKind = false)
                }
            }
            is SearchAction.RunQuery -> {
                debounceJob?.cancel()
                setQuery(action.value)
                runSearch(keepKind = false)
            }
            is SearchAction.SelectKind -> {
                savedState["kind"] = action.kind?.name
                updateState { it.copy(selectedKind = action.kind, visibleLimit = SEARCH_LIMIT) }
            }
            is SearchAction.Open -> {
                recentSearches.add(state.value.query)
                updateState { it.copy(recent = recentSearches.get()) }
                sendEvent(SearchEvent.HideKeyboard)
                navigator.navigateTo(routeFor(action.hit.item))
            }
            SearchAction.BrowseExplore -> navigator.switchTab(BottomTab.Explore)
            SearchAction.Back -> navigator.navigateUp()
            SearchAction.ShowMore -> updateState { it.copy(visibleLimit = it.visibleLimit + SEARCH_LIMIT) }
            SearchAction.ClearRecent -> {
                recentSearches.clear()
                updateState { it.copy(recent = emptyList()) }
            }
            SearchAction.Retry -> if (loadJob?.isActive != true) loadJob = viewModelScope.launch { load() }
        }
    }

    private fun setQuery(value: String) {
        savedState["query"] = value
        updateState { it.copy(query = value) }
    }

    private suspend fun load() {
        if (index.items.isEmpty()) updateState { it.copy(indexStatus = SearchIndexStatus.LOADING) }
        buildIndex(language)
            .onSuccess { built ->
                index = built
                updateState { it.copy(indexStatus = SearchIndexStatus.READY, isIncomplete = !built.isComplete) }
            }
            .onFailure {
                // Keep a previously built index (e.g. retry while offline); only an empty one is "not ready".
                if (index.items.isEmpty()) updateState { it.copy(indexStatus = SearchIndexStatus.NOT_READY) }
            }
        // Restored or language changed: recompute for the current query, keeping the chosen chip.
        runSearch(keepKind = true)
    }

    private fun runSearch(keepKind: Boolean) {
        val query = state.value.query
        val hits = searchIndex(query, index)
        updateState { current ->
            val kind = current.selectedKind?.takeIf { keepKind && hits.any { h -> h.item.kind == it } }
            if (!keepKind || kind != current.selectedKind) savedState["kind"] = kind?.name
            current.copy(searchedQuery = query, hits = hits, selectedKind = kind, visibleLimit = if (keepKind) current.visibleLimit else SEARCH_LIMIT)
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 150L
    }
}

/** Where a result opens: its own detail, or the exact Essentials module. */
internal fun routeFor(item: SearchIndexItem): Screen = when (item.kind) {
    SearchKind.PLACE -> Screen.PlaceDetailScreen(item.id)
    SearchKind.ACTIVITY -> Screen.ExperienceDetailScreen(item.id)
    SearchKind.STORY -> Screen.LocalFindDetailScreen(item.id)
    SearchKind.ESSENTIAL -> Screen.EssentialModuleScreen(item.id)
    SearchKind.EVENT -> Screen.EventDetailScreen(item.id)
    // The map on that point's own category, with the exact point selected (by ID, never by name).
    SearchKind.UTILITY -> Screen.UtilityMapScreen(
        item.utilityCategory?.name ?: ALL_CATEGORIES,
        item.id.takeUnless { item.pointCount != null }
    )
    SearchKind.SERVICE -> Screen.ServiceDetailScreen(item.id)
    SearchKind.NOTICE -> Screen.InfoCenterDetailScreen(item.id)
}
