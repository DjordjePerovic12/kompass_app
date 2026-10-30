package llc.bokadev.kompass.presentation.essentials.utility_map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.essentials_couldnt_load_utilities
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.ALL_CATEGORIES
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.getOrNull
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Utility
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.domain.model.distanceKmTo
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.usecase.GetUtilitiesUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.essentials.utils.KOTOR_OLD_TOWN_CENTER
import llc.bokadev.kompass.presentation.essentials.utils.UtilityLocationItem
import llc.bokadev.kompass.presentation.essentials.utils.buildUtilityLocationItems
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

/** The route's "category" and "selectedId" arrive through the [SavedStateHandle]. */
class UtilityMapViewModel(
    savedStateHandle: SavedStateHandle,
    private val getUtilities: GetUtilitiesUseCase,
    private val userLocationProvider: UserLocationProvider,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<UtilityMapState, UtilityMapEvent>() {

    override val initialViewState = savedStateHandle.get<String>("category").let { arg ->
        UtilityMapState(
            category = arg?.let { runCatching { UtilityCategory.valueOf(it) }.getOrNull() },
            showAll = arg == ALL_CATEGORIES
        )
    }
    private var hasLoadedInitialData = false

    private var cachedUtilities: List<Utility> = emptyList()
    private var loadJob: Job? = null
    private val requestedSelection = savedStateHandle.get<String>("selectedId")

    init {
        startLocationRefresh()
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val utilityMapState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                analytics.trackScreenView("utility_map")
                load()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: UtilityMapEvent) {
        when (event) {
            is UtilityMapEvent.OnUtilitySelect -> state.update { it.copy(selectedUtilityId = event.id, selectionGone = false) }
            is UtilityMapEvent.OnNavigateClick -> state.value.items.firstOrNull { it.utility.id == event.id }?.let { item ->
                openExternal(ExternalLaunch.Maps(item.point.latitude, item.point.longitude, item.utility.name))
            }

            UtilityMapEvent.OnRetryClick -> load()
            UtilityMapEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            is UtilityMapEvent.OpenExternal -> Unit
        }
    }

    private fun openExternal(launch: ExternalLaunch) {
        viewModelScope.launch {
            event.emit(UtilityMapEvent.OpenExternal(launch))
        }
    }

    private fun load() {
        if (state.value.category == null && !state.value.showAll) return

        // A retry supersedes an older load so a slow response can't overwrite a newer one.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // Keep the current map/list on screen while reloading; only a first load shows progress.
            state.update { it.copy(isLoading = it.items.isEmpty(), error = null, refreshFailed = false) }

            val currentLocation = runCatching { userLocationProvider.getCurrentLocation() }.getOrNull()
            val origin = currentLocation ?: KOTOR_OLD_TOWN_CENTER

            val utilitiesResult = getUtilities()
            utilitiesResult.getOrNull()?.let { cachedUtilities = it }
            val previousSelection = state.value.selectedUtilityId ?: requestedSelection
            val items = utilityMapItems(state.value.category, state.value.showAll, cachedUtilities, origin, previousSelection)
            val selection = resolveSelection(previousSelection, items)
            val failed = utilitiesResult is Resource.Error

            state.update {
                it.copy(
                    isLoading = false,
                    error = if (failed && items.isEmpty()) BaseError.InlineError(Res.string.essentials_couldnt_load_utilities) else null,
                    refreshFailed = failed && items.isNotEmpty(),
                    currentLocation = currentLocation,
                    items = items,
                    selectedUtilityId = selection.id,
                    selectionGone = selection.gone
                )
            }
        }
    }

    private fun startLocationRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5_000L)
                refreshLocationContext()
            }
        }
    }

    /** Updates distances and the "you" marker. Never changes the visitor's selection. */
    private suspend fun refreshLocationContext() {
        if (state.value.category == null && !state.value.showAll) return

        if (!userLocationProvider.hasPermission()) {
            state.update { current -> if (current.currentLocation == null) current else current.copy(currentLocation = null) }
            return
        }

        val currentLocation = runCatching { userLocationProvider.getCurrentLocation() }.getOrNull()
        val origin = currentLocation ?: KOTOR_OLD_TOWN_CENTER

        state.update { current ->
            current.copy(
                currentLocation = currentLocation,
                items = utilityMapItems(current.category, current.showAll, cachedUtilities, origin, current.selectedUtilityId)
            )
        }
    }
}

internal data class UtilitySelection(val id: String?, val gone: Boolean)

/**
 * Which point is selected after a load. A previous/requested selection is kept when it still
 * exists; if it vanished from the data the selection is cleared and flagged (never silently
 * swapped for an unrelated point). With nothing selected or requested, the nearest is picked.
 */
internal fun resolveSelection(previous: String?, items: List<UtilityLocationItem>): UtilitySelection = when {
    previous == null -> UtilitySelection(items.firstOrNull()?.utility?.id, gone = false)
    items.any { it.utility.id == previous } -> UtilitySelection(previous, gone = false)
    else -> UtilitySelection(null, gone = true)
}

/**
 * The nearest utilities for this map, always including [keepId] (the selected or requested
 * point) even when it ranks outside the nearest set, so moving never drops the selection.
 */
internal fun utilityMapItems(
    category: UtilityCategory?,
    showAll: Boolean,
    utilities: List<Utility>,
    origin: GeoPoint,
    keepId: String?
): List<UtilityLocationItem> {
    val ranked = if (showAll) {
        UtilityCategory.entries.flatMap { buildUtilityLocationItems(it, utilities, origin) }
            .sortedBy { it.distanceMeters ?: Int.MAX_VALUE }
    } else {
        category ?: return emptyList()
        buildUtilityLocationItems(category, utilities, origin)
    }
    val kept = keepId?.takeIf { id -> ranked.none { it.utility.id == id } }?.let { id ->
        utilities.firstOrNull { it.id == id }?.let { utility ->
            val lat = utility.latitude ?: return@let null
            val lng = utility.longitude ?: return@let null
            val point = GeoPoint(lat, lng)
            UtilityLocationItem(utility, point, (origin.distanceKmTo(point) * 1000).toInt())
        }
    }
    return ranked + listOfNotNull(kept)
}
