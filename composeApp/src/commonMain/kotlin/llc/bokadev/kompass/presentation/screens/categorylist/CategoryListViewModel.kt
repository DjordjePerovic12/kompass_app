package llc.bokadev.kompass.presentation.screens.categorylist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.presentation.navigation.Route
import llc.bokadev.kompass.domain.location.UserLocationProvider
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
import llc.bokadev.kompass.presentation.screens.favorites.REGION_RADIUS_KM
import llc.bokadev.kompass.presentation.screens.favorites.areaKeyFor

/** The Explore entry points this one list serves. Only the title, subtitle and data change. */
enum class CategoryListKind { PLACES, EAT_AND_DRINK, ACTIVITIES, MUST_SEE }

/** One row, already resolved. [distanceKm] only comes from the device's real location. */
data class ListItem(
    val type: FavoriteItemType,
    val id: String,
    val place: Place? = null,
    val activity: Experience? = null,
    val areaKey: String,
    val distanceKm: Double?,
    val sortOrder: Int
)

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
) : BaseViewModel<CategoryListState, CategoryListAction, CategoryListEvent>(
    CategoryListState(kind = kindOf(kindArg ?: savedState.get<String>("kind")), areaFilter = savedState.get<String>("area"))
) {

    private val kind = state.value.kind
    private var all: List<ListItem> = emptyList()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch { favoritesRepository.favoritesFlow.collect { rebuild() } }
    }

    override suspend fun onFirstStart() = load()

    override fun onAction(action: CategoryListAction) {
        when (action) {
            is CategoryListAction.SetSaved -> setSaved(action.item, action.saved)
            is CategoryListAction.Undo -> favoritesRepository.restore(action.entry)
            is CategoryListAction.SetArea -> {
                savedState["area"] = action.areaKey
                updateState { it.copy(areaFilter = action.areaKey) }
                rebuild()
            }
            is CategoryListAction.Open -> navigator.navigateTo(
                if (action.item.type == FavoriteItemType.PLACE) Route.PlaceDetail(action.item.id) else Route.ExperienceDetail(action.item.id)
            )
            CategoryListAction.OpenSearch -> navigator.navigateTo(Route.Search)
            CategoryListAction.Retry -> load()
            CategoryListAction.Back -> navigator.navigateUp()
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
            sendEvent(CategoryListEvent.Saved(item))
        } else {
            favoritesRepository.remove(item.type, item.id)?.let { sendEvent(CategoryListEvent.Removed(item, it)) }
        }
    }

    private fun load() {
        // A retry supersedes an older load, so a stale response can't overwrite a newer one.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = all.isEmpty(), error = null) }
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
                    updateState { it.copy(isLoading = false, refreshFailed = false) }
                }
                .onFailure {
                    updateState { it.copy(isLoading = false, refreshFailed = all.isNotEmpty(), error = if (all.isEmpty()) LOAD_FAILED else null) }
                }
            rebuild()
        }
    }

    private fun rebuild() {
        val savedKeys = favoritesRepository.favoritesFlow.value.map { FavoriteKey(it.type, it.id) }.toSet()
        updateState { it.copy(items = orderedFor(all, state.value.areaFilter), savedKeys = savedKeys, areas = areasOf(all)) }
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

private val LOAD_FAILED = UiText { it.couldNotLoadCategories }

private fun kindOf(value: String?): CategoryListKind =
    value?.let { runCatching { CategoryListKind.valueOf(it) }.getOrNull() } ?: CategoryListKind.PLACES
