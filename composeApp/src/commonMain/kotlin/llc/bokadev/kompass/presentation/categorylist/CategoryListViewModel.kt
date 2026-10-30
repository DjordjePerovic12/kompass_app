package llc.bokadev.kompass.presentation.categorylist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.could_not_load_categories
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.networking.map
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.CategoryListKind
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.KotorOldTownCenter
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.model.distanceKmTo
import llc.bokadev.kompass.domain.model.toSavedSnapshot
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.PlaceRepository
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.screens.favorites.REGION_RADIUS_KM
import llc.bokadev.kompass.presentation.screens.favorites.areaKeyFor

class CategoryListViewModel(
    private val placeRepository: PlaceRepository,
    private val getActivities: GetActivitiesUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val userLocationProvider: UserLocationProvider,
    private val savedState: SavedStateHandle,
    private val snapshots: SavedSnapshotRepository,
    private val navigator: Navigator,
    /** The list kind from the caller; legacy routes don't carry a "kind" argument. */
    kindArg: String? = null
) : BaseViewModel<CategoryListState, CategoryListEvent>() {

    override val initialViewState = CategoryListState(
        kind = kindOf(kindArg ?: savedState.get<String>("kind")),
        areaFilter = savedState.get<String>("area")
    )
    private val kind = initialViewState.kind
    private var all: List<ListItem> = emptyList()
    private var loadJob: Job? = null
    private var hasLoadedInitialData = false

    init {
        viewModelScope.launch { favoritesRepository.favoritesFlow.collect { rebuild() } }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val categoryListState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                load()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: CategoryListEvent) {
        when (event) {
            CategoryListEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            CategoryListEvent.OnRetryClick -> load()
            CategoryListEvent.OnSearchClick -> viewModelScope.launch {
                navigator.navigateTo(Screen.SearchScreen)
            }

            is CategoryListEvent.OnItemClick -> viewModelScope.launch {
                navigator.navigateTo(
                    if (event.item.type == FavoriteItemType.PLACE) Screen.PlaceDetailScreen(event.item.id)
                    else Screen.ExperienceDetailScreen(event.item.id)
                )
            }

            is CategoryListEvent.OnSavedChange -> setSaved(event.item, event.saved)
            is CategoryListEvent.OnAreaChange -> {
                savedState["area"] = event.areaKey
                state.update { it.copy(areaFilter = event.areaKey) }
                rebuild()
            }

            is CategoryListEvent.OnUndoClick -> favoritesRepository.restore(event.entry)
            is CategoryListEvent.ShowSaved, is CategoryListEvent.ShowRemoved -> Unit
        }
    }

    /** Persists immediately (no animation delay) and is idempotent; only a real change confirms. */
    private fun setSaved(item: ListItem, saved: Boolean) {
        if (saved) {
            if (favoritesRepository.isFavorited(item.type, item.id)) return
            // Remember the title/photo/area now, so Saved can show it offline before it ever resolves.
            (item.place?.toSavedSnapshot(item.areaKey) ?: item.activity?.toSavedSnapshot(item.areaKey))
                ?.let { snapshots.putAll(mapOf(FavoriteKey(item.type, item.id) to it)) }
            favoritesRepository.save(item.type, item.id)
            emit(CategoryListEvent.ShowSaved(item))
        } else {
            favoritesRepository.remove(item.type, item.id)?.let { emit(CategoryListEvent.ShowRemoved(item, it)) }
        }
    }

    private fun emit(uiEvent: CategoryListEvent) {
        viewModelScope.launch {
            event.emit(uiEvent)
        }
    }

    private fun load() {
        // A retry supersedes an older load, so a stale response can't overwrite a newer one.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoading = all.isEmpty(), error = null) }
            // Distances only in the bay (same 30 km rule as Saved); elsewhere they mean nothing.
            val location = async {
                if (userLocationProvider.hasPermission()) runCatching { userLocationProvider.getCurrentLocation() }.getOrNull() else null
            }.let { deferred -> async { deferred.await()?.takeIf { KotorOldTownCenter.distanceKmTo(it) <= REGION_RADIUS_KM } } }
            val result = when (kind) {
                CategoryListKind.ACTIVITIES -> getActivities().map { list -> list.map { it.toItem(location.await()) } }
                else -> placeRepository.getActivePlaces().map { places -> placesFor(kind, places, location.await()) }
            }
            result
                .onSuccess { items ->
                    all = items
                    state.update { it.copy(isLoading = false, refreshFailed = false) }
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            refreshFailed = all.isNotEmpty(),
                            error = if (all.isEmpty()) BaseError.InlineError(Res.string.could_not_load_categories) else null
                        )
                    }
                }
            rebuild()
        }
    }

    private fun rebuild() {
        val savedKeys = favoritesRepository.favoritesFlow.value.map { FavoriteKey(it.type, it.id) }.toSet()
        state.update { it.copy(items = orderedFor(all, it.areaFilter), savedKeys = savedKeys, areas = areasOf(all)) }
    }
}

/** The rows for a place-based list, with distances only when the device location is real. */
internal fun placesFor(kind: CategoryListKind, places: List<Place>, location: GeoPoint?): List<ListItem> {
    return places
        .filter { place ->
            when (kind) {
                CategoryListKind.PLACES -> place.category == PlaceCategory.SEE_AND_VISIT
                CategoryListKind.EAT_AND_DRINK -> place.category == PlaceCategory.EAT_AND_DRINK
                CategoryListKind.MUST_SEE -> place.isMustSee
                CategoryListKind.ACTIVITIES -> false
            }
        }
        .map { it.toItem(location) }
}

private fun Place.toItem(location: GeoPoint?): ListItem {
    val point = GeoPoint(latitude, longitude)
    return ListItem(FavoriteItemType.PLACE, id, place = this, areaKey = areaKeyFor(zone), distanceKm = location?.distanceKmTo(point), sortOrder = sortOrder)
}

private fun Experience.toItem(location: GeoPoint?): ListItem {
    val point = latitude?.let { lat -> longitude?.let { GeoPoint(lat, it) } }
    return ListItem(
        FavoriteItemType.ACTIVITY, id, activity = this,
        areaKey = areaKeyFor(localizedLocation("en")),
        distanceKm = point?.let { p -> location?.distanceKmTo(p) },
        sortOrder = sortOrder
    )
}

/** The rows in the area filter, by proximity when distances exist, else editorial order. Saved state doesn't affect order. */
internal fun orderedFor(items: List<ListItem>, areaFilter: String?): List<ListItem> =
    items.filter { areaFilter == null || it.areaKey == areaFilter }
        .sortedWith(compareBy<ListItem> { it.distanceKm ?: Double.MAX_VALUE }.thenBy { it.sortOrder })

/** Area filter options present in this list, in the same visiting order as the Saved tab. */
internal fun areasOf(items: List<ListItem>): List<String> {
    val order = listOf("old_town", "dobrota", "perast")
    return items.map { it.areaKey }.distinct().sortedWith(
        compareBy<String> { order.indexOf(it).takeIf { i -> i >= 0 } ?: 100 }.thenBy { it }
    )
}

private fun kindOf(value: String?): CategoryListKind =
    value?.let { runCatching { CategoryListKind.valueOf(it) }.getOrNull() } ?: CategoryListKind.PLACES
