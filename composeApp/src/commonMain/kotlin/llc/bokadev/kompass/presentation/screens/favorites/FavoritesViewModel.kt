package llc.bokadev.kompass.presentation.screens.favorites

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.KotorOldTownCenter
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.SavedSnapshot
import llc.bokadev.kompass.domain.model.distanceKmTo
import llc.bokadev.kompass.domain.model.toSavedSnapshot
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.PlaceRepository
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase
import llc.bokadev.kompass.presentation.screens.home.HomeArea

/** "In the bay": within this distance of Kotor Old Town, distances are shown; outside, never. */
internal const val REGION_RADIUS_KM = 30.0

/** The old distance-based bucket; only found in snapshots saved before the pilot. */
private const val LEGACY_GROUP_WORTH_THE_DRIVE = "worth_the_drive"
const val GROUP_OTHER = "other"

/** How far a saved entry could be resolved against current content. */
enum class SavedItemStatus {
    /** Found in the loaded content. */
    RESOLVED,
    /** Content is still loading; shown from its snapshot meanwhile. */
    LOADING,
    /** Content couldn't be fetched (offline/failure); shown from its snapshot, never called unavailable. */
    NOT_REFRESHED,
    /** The content list loaded fine and this id isn't in it any more. */
    UNAVAILABLE
}

/** One saved entry, resolved as far as the data allows. */
data class SavedItem(
    val entry: FavoriteEntry,
    val place: Place? = null,
    val activity: Experience? = null,
    /** Last-known name/photo, used when the item can't be resolved (offline or unpublished). */
    val snapshot: SavedSnapshot? = null,
    val status: SavedItemStatus = SavedItemStatus.RESOLVED,
    /** Null unless the user is in the bay. */
    val distanceKm: Double? = null,
    val groupKey: String = GROUP_OTHER
) {
    val unavailable: Boolean get() = status == SavedItemStatus.UNAVAILABLE
}

data class SavedGroup(val key: String, val items: List<SavedItem>)

class FavoritesViewModel(
    private val placeRepository: PlaceRepository,
    private val getActivities: GetActivitiesUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val userLocationProvider: UserLocationProvider,
    private val snapshots: SavedSnapshotRepository,
    private val navigator: Navigator,
    private val languageSheet: LanguageSheetHost
) : BaseViewModel<FavoritesState, FavoritesAction, FavoritesEvent>(FavoritesState()) {

    private var places: List<Place>? = null
    private var activities: List<Experience>? = null
    private var location: GeoPoint? = null
    /** True once a load has finished (whatever its outcome); before that, unresolved means loading. */
    private var loadFinished = false
    private var loadJob: Job? = null

    init {
        viewModelScope.launch { favoritesRepository.favoritesFlow.collect { rebuild() } }
    }

    override suspend fun onFirstStart() = load()

    override fun onAction(action: FavoritesAction) {
        when (action) {
            FavoritesAction.Load -> load()
            // Idempotent: a repeated Remove can't re-add the item.
            is FavoritesAction.Remove -> favoritesRepository.remove(action.entry.type, action.entry.id)?.let {
                sendEvent(FavoritesEvent.Removed(it, action.title))
            }
            is FavoritesAction.Undo -> favoritesRepository.restore(action.entry)
            is FavoritesAction.Open -> navigator.navigateTo(
                if (action.type == FavoriteItemType.PLACE) Screen.PlaceDetailScreen(action.id) else Screen.ExperienceDetailScreen(action.id)
            )
            FavoritesAction.ExploreKotor -> navigator.switchTab(BottomTab.Explore)
            FavoritesAction.OpenLanguage -> languageSheet.open()
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
        updateState {
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
 * The geographic area an item is grouped under: a curated Home area, else its own normalized area
 * ("Kotor Bay" and "kotor_bay" are one group), else Other. Never distance-based: Worth the Drive is an
 * editorial collection (`worth_the_drive_items`), not a geographic bucket. Idempotent, so stored keys
 * can be passed back through it; the pre-pilot "worth_the_drive" bucket becomes Other.
 */
internal fun areaKeyFor(area: String?): String {
    HomeArea.entries.filter { it != HomeArea.DEVICE }.firstOrNull { it.matchesZone(area) }?.let { return it.preferenceValue }
    val normalized = area?.lowercase()?.replace(Regex("[^\\p{L}\\p{N}]+"), "_")?.trim('_')
    return normalized?.takeIf { it.isNotEmpty() && it != LEGACY_GROUP_WORTH_THE_DRIVE } ?: GROUP_OTHER
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
