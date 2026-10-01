package llc.bokadev.kompass.presentation.screens.search

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.SearchEmptyStateKind
import llc.bokadev.kompass.domain.model.SearchFilterType
import llc.bokadev.kompass.domain.model.SearchResult
import llc.bokadev.kompass.domain.model.favoriteAndNearestFirst
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.SearchRepository
import llc.bokadev.kompass.data.repository.SearchOfflineMissException
import llc.bokadev.kompass.data.repository.SearchServiceException
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class SearchState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val query: String = "",
    val selectedType: SearchFilterType = SearchFilterType.ALL,
    val results: List<SearchListItem> = emptyList(),
    val isOffline: Boolean = false,
    val offlineCacheAvailable: Boolean = false,
    val emptyStateKind: SearchEmptyStateKind = SearchEmptyStateKind.NONE
) : BaseState()

sealed interface SearchEvent : BaseEvent {
    data class QueryChanged(val value: String) : SearchEvent
    data class TypeChanged(val type: SearchFilterType) : SearchEvent
    data object Refresh : SearchEvent
}

data class SearchListItem(
    val result: SearchResult,
    val distanceKm: Double?
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val favoritesRepository: FavoritesRepository,
    private val userLocationProvider: UserLocationProvider
) : BaseViewModel<SearchState, SearchEvent>() {

    override val initialState = SearchState()

    private var searchJob: Job? = null
    private var rawResults: List<SearchResult> = emptyList()
    private var currentLanguage: String = "en"
    private var origin: GeoPoint = KOTOR_OLD_TOWN_CENTER

    init {
        viewModelScope.launch {
            origin = userLocationProvider.getCurrentLocation() ?: KOTOR_OLD_TOWN_CENTER
            applyVisibleResults()
        }
        viewModelScope.launch {
            favoritesRepository.favoritesFlow.collect {
                applyVisibleResults()
            }
        }
    }

    fun load(language: String) {
        currentLanguage = language
        val cached = searchRepository.getCachedSearch(language)
        rawResults = cached?.results.orEmpty()
        _state.update {
            it.copy(
                results = orderResults(rawResults),
                offlineCacheAvailable = cached != null,
                isOffline = false,
                emptyStateKind = if (rawResults.isEmpty()) SearchEmptyStateKind.NONE else SearchEmptyStateKind.NONE
            )
        }
    }

    override fun onIntent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> onQueryChanged(event.value)
            is SearchEvent.TypeChanged -> {
                _state.update { it.copy(selectedType = event.type) }
                applyVisibleResults()
            }
            SearchEvent.Refresh -> performSearch(_state.value.query)
        }
    }

    fun toggleFavorite(key: FavoriteKey) {
        favoritesRepository.toggleFavorite(key.type, key.id)
    }

    private fun onQueryChanged(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            performSearch(value)
        }
    }

    private fun performSearch(query: String) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            rawResults = searchRepository.getCachedSearch(currentLanguage)?.results.orEmpty()
            _state.update {
                it.copy(
                    isLoading = false,
                    error = null,
                    isOffline = false,
                    offlineCacheAvailable = searchRepository.getCachedSearch(currentLanguage) != null,
                    emptyStateKind = SearchEmptyStateKind.NONE
                )
            }
            applyVisibleResults()
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            searchRepository.search(
                query = normalizedQuery,
                language = currentLanguage,
                type = SearchFilterType.ALL
            ).fold(
                onSuccess = { response ->
                    rawResults = response.results
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isOffline = response.fromCache,
                            offlineCacheAvailable = searchRepository.getCachedSearch(currentLanguage) != null,
                            emptyStateKind = when {
                                response.offlineRequiresInternet -> SearchEmptyStateKind.OFFLINE_REQUIRES_INTERNET
                                response.results.isEmpty() -> SearchEmptyStateKind.NO_RESULTS
                                else -> SearchEmptyStateKind.NONE
                            }
                        )
                    }
                    applyVisibleResults()
                },
                onFailure = { error ->
                    rawResults = emptyList()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = when (error) {
                                is SearchServiceException -> "Search service is not available right now."
                                else -> "Connect to the internet to run a new search."
                            },
                            isOffline = error is SearchOfflineMissException,
                            offlineCacheAvailable = searchRepository.getCachedSearch(currentLanguage) != null,
                            emptyStateKind = when (error) {
                                is SearchServiceException -> SearchEmptyStateKind.NONE
                                else -> SearchEmptyStateKind.OFFLINE_REQUIRES_INTERNET
                            },
                            results = emptyList()
                        )
                    }
                }
            )
        }
    }

    private fun applyVisibleResults() {
        _state.update { current ->
            current.copy(
                results = orderResults(rawResults, current.selectedType)
            )
        }
    }

    private fun orderResults(
        input: List<SearchResult>,
        filter: SearchFilterType = _state.value.selectedType
    ): List<SearchListItem> {
        val filtered = when (filter) {
            SearchFilterType.ALL -> input
            SearchFilterType.PLACE -> input.filter { it.type == llc.bokadev.kompass.domain.model.SearchResultType.PLACE }
            SearchFilterType.ACTIVITY -> input.filter { it.type == llc.bokadev.kompass.domain.model.SearchResultType.ACTIVITY }
            SearchFilterType.EVENT -> input.filter { it.type == llc.bokadev.kompass.domain.model.SearchResultType.EVENT }
        }

        val ordered = filtered.favoriteAndNearestFirst(
            favoriteKeys = favoritesRepository.getFavoriteKeySet(),
            origin = origin
        )

        return ordered.map { result ->
            SearchListItem(
                result = result,
                distanceKm = if (result.latitude != null && result.longitude != null) {
                    haversineDistanceKm(origin, GeoPoint(result.latitude, result.longitude))
                } else {
                    null
                }
            )
        }
    }

    private fun haversineDistanceKm(from: GeoPoint, to: GeoPoint): Double {
        val earthRadiusKm = 6371.0
        val dLat = (to.latitude - from.latitude).toRadians()
        val dLon = (to.longitude - from.longitude).toRadians()
        val fromLat = from.latitude.toRadians()
        val toLat = to.latitude.toRadians()

        val a = sin(dLat / 2).pow(2) +
            cos(fromLat) * cos(toLat) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return earthRadiusKm * c
    }

    private fun Double.toRadians(): Double = this * (kotlin.math.PI / 180.0)

    private companion object {
        val KOTOR_OLD_TOWN_CENTER = GeoPoint(
            latitude = 42.4246,
            longitude = 18.7712
        )
    }
}
