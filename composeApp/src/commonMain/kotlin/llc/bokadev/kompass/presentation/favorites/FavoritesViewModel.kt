package llc.bokadev.kompass.presentation.favorites

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.KotorOldTownCenter
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.distanceKmTo
import llc.bokadev.kompass.domain.model.toSavedSnapshot
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.PlaceRepository
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.HomeArea
import llc.bokadev.kompass.presentation.shared.GROUP_OTHER
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.presentation.shared.REGION_RADIUS_KM
import llc.bokadev.kompass.presentation.shared.areaKeyFor

class FavoritesViewModel(
    private val placeRepository: PlaceRepository,
    private val getActivities: GetActivitiesUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val userLocationProvider: UserLocationProvider,
    private val snapshots: SavedSnapshotRepository,
    private val navigator: Navigator,
    private val languageSheet: LanguageSheetHost
) : BaseViewModel<FavoritesState, FavoritesEvent>() {

    override val initialViewState = FavoritesState()
    private var places: List<Place>? = null
    private var activities: List<Experience>? = null
    private var location: GeoPoint? = null
    /** True once a load has finished (whatever its outcome); before that, unresolved means loading. */
    private var loadFinished = false
    private var loadJob: Job? = null
    private var hasLoadedInitialData = false

    init {
        viewModelScope.launch { favoritesRepository.favoritesFlow.collect { rebuild() } }
    }

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val favoritesState = state
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

    fun onEvent(event: FavoritesEvent) {
        when (event) {
            FavoritesEvent.OnRetryClick -> load()
            // Idempotent: a repeated remove can't re-add the item.
            is FavoritesEvent.OnRemoveClick -> favoritesRepository.remove(event.entry.type, event.entry.id)?.let {
                emit(FavoritesEvent.ShowRemoved(it, event.title))
            }

            is FavoritesEvent.OnUndoClick -> favoritesRepository.restore(event.entry)
            is FavoritesEvent.OnItemClick -> viewModelScope.launch {
                navigator.navigateTo(
                    if (event.type == FavoriteItemType.PLACE) Screen.PlaceDetailScreen(event.id) else Screen.ExperienceDetailScreen(event.id)
                )
            }

            FavoritesEvent.OnExploreKotorClick -> viewModelScope.launch {
                navigator.switchTab(BottomTab.Explore)
            }

            FavoritesEvent.OnLanguageClick -> languageSheet.open()
            is FavoritesEvent.ShowRemoved -> Unit
        }
    }

    private fun emit(uiEvent: FavoritesEvent) {
        viewModelScope.launch {
            event.emit(uiEvent)
        }
    }

    private fun load() {
        // A retry supersedes an older load, so a stale response can't overwrite a newer one.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val placesResult = async { placeRepository.getActivePlaces() }
            val activitiesResult = async { getActivities() }
            val locationResult = async {
                if (userLocationProvider.hasPermission()) runCatching { userLocationProvider.getCurrentLocation() }.getOrNull() else null
            }
            placesResult.await().onSuccess { places = it }
            activitiesResult.await().onSuccess { activities = it }
            location = locationResult.await()
            loadFinished = true
            rebuild()
        }
    }

    private fun rebuild() {
        val inRegion = location?.let { KotorOldTownCenter.distanceKmTo(it) <= REGION_RADIUS_KM } == true
        val items = favoritesRepository.favoritesFlow.value.map { entry -> resolve(entry, if (inRegion) location else null) }
        snapshots.putAll(
            items.mapNotNull { item ->
                val snapshot = item.place?.toSavedSnapshot(item.groupKey) ?: item.activity?.toSavedSnapshot(item.groupKey)
                snapshot?.let { FavoriteKey(item.entry.type, item.entry.id) to it }
            }.toMap()
        )
        state.update {
            it.copy(
                // With nothing saved there's nothing to wait for; otherwise "done" only after the load.
                isLoading = !loadFinished && items.isNotEmpty(),
                groups = groupSaved(items, inRegion),
                inRegion = inRegion,
                refreshFailed = items.any { item -> item.status == SavedItemStatus.NOT_REFRESHED }
            )
        }
    }

    private fun unresolvedStatus(listLoaded: Boolean): SavedItemStatus = when {
        listLoaded -> SavedItemStatus.UNAVAILABLE
        loadFinished -> SavedItemStatus.NOT_REFRESHED
        else -> SavedItemStatus.LOADING
    }

    private fun resolve(entry: FavoriteEntry, origin: GeoPoint?): SavedItem {
        val snapshot = snapshots.get(FavoriteKey(entry.type, entry.id))
        return when (entry.type) {
            FavoriteItemType.PLACE -> {
                val list = places
                val place = list?.firstOrNull { it.id == entry.id }
                if (place == null) {
                    SavedItem(entry, snapshot = snapshot, status = unresolvedStatus(list != null), groupKey = areaKeyFor(snapshot?.groupKey))
                } else {
                    val point = GeoPoint(place.latitude, place.longitude)
                    SavedItem(entry, place = place, snapshot = snapshot, distanceKm = origin?.distanceKmTo(point), groupKey = areaKeyFor(place.zone))
                }
            }

            FavoriteItemType.ACTIVITY -> {
                val list = activities
                val activity = list?.firstOrNull { it.id == entry.id }
                if (activity == null) {
                    SavedItem(entry, snapshot = snapshot, status = unresolvedStatus(list != null), groupKey = areaKeyFor(snapshot?.groupKey))
                } else {
                    val point = activity.latitude?.let { lat -> activity.longitude?.let { GeoPoint(lat, it) } }
                    SavedItem(
                        entry, activity = activity, snapshot = snapshot,
                        distanceKm = point?.let { p -> origin?.distanceKmTo(p) },
                        groupKey = areaKeyFor(activity.localizedLocation("en"))
                    )
                }
            }
        }
    }
}

/**
 * Groups in visiting order: the user's current area first (the group holding their nearest item,
 * only when in the bay), then Old Town, Dobrota, Perast, other areas A–Z, Other.
 * Inside a group: most recently saved first.
 */
internal fun groupSaved(items: List<SavedItem>, inRegion: Boolean): List<SavedGroup> {
    val curated = listOf(HomeArea.OLD_TOWN, HomeArea.DOBROTA, HomeArea.PERAST).map { it.preferenceValue }
    val groups = items.groupBy { it.groupKey }.map { (key, groupItems) ->
        SavedGroup(key, groupItems.sortedByDescending { it.entry.favoritedAtEpochMs })
    }
    val current = if (inRegion) items.filter { it.distanceKm != null }.minByOrNull { it.distanceKm!! }?.groupKey else null
    return groups.sortedWith(
        compareBy<SavedGroup> { if (it.key == current) 0 else 1 }
            .thenBy {
                when (it.key) {
                    in curated -> curated.indexOf(it.key)
                    GROUP_OTHER -> 1_000
                    else -> 100
                }
            }
            .thenBy { it.key }
    )
}
