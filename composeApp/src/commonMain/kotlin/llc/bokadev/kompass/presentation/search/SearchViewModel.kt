package llc.bokadev.kompass.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.ALL_CATEGORIES
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.model.SEARCH_LIMIT
import llc.bokadev.kompass.domain.model.SearchIndex
import llc.bokadev.kompass.domain.model.SearchIndexItem
import llc.bokadev.kompass.domain.model.SearchKind
import llc.bokadev.kompass.domain.model.searchIndex
import llc.bokadev.kompass.domain.repository.RecentSearchesRepository
import llc.bokadev.kompass.presentation.base.BaseViewModel

/**
 * Local, destination-only search (Search spec): an in-memory index of cached content, rebuilt
 * when the app language changes. No server call per keystroke; works offline once content exists.
 */
class SearchViewModel(
    /** [llc.bokadev.kompass.domain.usecase.BuildSearchIndexUseCase] in the app; a lambda so tests can hand in an index directly. */
    private val buildIndex: suspend (language: String) -> Resource<SearchIndex, NetworkError, String?>,
    private val recentSearches: RecentSearchesRepository,
    private val preferences: AppPreferences,
    private val savedState: SavedStateHandle,
    private val navigator: Navigator
) : BaseViewModel<SearchState, SearchEvent>() {

    override val initialViewState = SearchState(
        query = savedState.get<String>("query").orEmpty(),
        selectedKind = savedState.get<String>("kind")?.let { k -> SearchKind.entries.firstOrNull { it.name == k } },
        recent = recentSearches.get()
    )
    private var index: SearchIndex = SearchIndex(emptyList())
    private var loadJob: Job? = null
    private var debounceJob: Job? = null
    private var language: String = preferences.getSelectedLanguage()
    private var hasLoadedInitialData = false

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val searchState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeLanguage()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChange -> {
                setQuery(event.value)
                debounceJob?.cancel()
                debounceJob = viewModelScope.launch {
                    delay(DEBOUNCE_MS)
                    runSearch(keepKind = false)
                }
            }

            is SearchEvent.OnQuerySubmit -> {
                debounceJob?.cancel()
                setQuery(event.value)
                runSearch(keepKind = false)
            }

            is SearchEvent.OnKindChange -> {
                savedState["kind"] = event.kind?.name
                state.update { it.copy(selectedKind = event.kind, visibleLimit = SEARCH_LIMIT) }
            }

            is SearchEvent.OnResultClick -> {
                recentSearches.add(state.value.query)
                state.update { it.copy(recent = recentSearches.get()) }
                viewModelScope.launch {
                    this@SearchViewModel.event.emit(SearchEvent.HideKeyboard)
                    navigator.navigateTo(routeFor(event.hit.item))
                }
            }

            SearchEvent.OnBrowseExploreClick -> viewModelScope.launch {
                navigator.switchTab(BottomTab.Explore)
            }

            SearchEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            SearchEvent.OnShowMoreClick -> state.update { it.copy(visibleLimit = it.visibleLimit + SEARCH_LIMIT) }
            SearchEvent.OnClearRecentClick -> {
                recentSearches.clear()
                state.update { it.copy(recent = emptyList()) }
            }

            SearchEvent.OnRetryClick -> if (loadJob?.isActive != true) loadJob = viewModelScope.launch { load() }
            SearchEvent.HideKeyboard -> Unit
        }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            preferences.selectedLanguageFlow.collect { lang ->
                // A new language rebuilds the index: names, categories and vocabulary are all localized.
                language = lang
                loadJob?.cancel()
                loadJob = launch { load() }
            }
        }
    }

    private fun setQuery(value: String) {
        savedState["query"] = value
        state.update { it.copy(query = value) }
    }

    private suspend fun load() {
        if (index.items.isEmpty()) state.update { it.copy(indexStatus = SearchIndexStatus.LOADING) }
        buildIndex(language)
            .onSuccess { built ->
                index = built
                state.update { it.copy(indexStatus = SearchIndexStatus.READY, isIncomplete = !built.isComplete) }
            }
            .onError { _, _ ->
                // Keep a previously built index (e.g. retry while offline); only an empty one is "not ready".
                if (index.items.isEmpty()) state.update { it.copy(indexStatus = SearchIndexStatus.NOT_READY) }
            }
        // Restored or language changed: recompute for the current query, keeping the chosen chip.
        runSearch(keepKind = true)
    }

    private fun runSearch(keepKind: Boolean) {
        val query = state.value.query
        val hits = searchIndex(query, index)
        state.update { current ->
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
