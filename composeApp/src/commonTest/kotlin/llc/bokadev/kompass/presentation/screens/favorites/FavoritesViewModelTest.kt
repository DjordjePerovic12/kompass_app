package llc.bokadev.kompass.presentation.screens.favorites

import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.testutil.RecordingNavigator
import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val oldTown = place("cathedral", "old_town", 42.4246, 18.7712)
    private val perast = place("rocks", "perast", 42.4868, 18.6982)
    private val lovcen = place("lovcen", "lovcen", 42.3990, 18.8390) // ~7 km out, no curated zone

    @Test fun groupsFollowAreaOrderAndNewestFirst() = run(
        favorites = listOf(entry("rocks", 1), entry("cathedral", 2), entry("lovcen", 3), entry("cathedral2", 4)),
        places = listOf(oldTown, perast, lovcen, place("cathedral2", "kotor", 42.4247, 18.7713))
    ) { vm, _ ->
        val groups = vm.state.value.groups
        assertEquals(listOf("old_town", "perast", "lovcen"), groups.map { it.key })
        assertEquals(listOf("cathedral2", "cathedral"), groups.first().items.map { it.entry.id })
    }

    @Test fun farPlacesGroupByTheirOwnAreaNeverAsWorthTheDrive() {
        assertEquals("lovcen", areaKeyFor("lovcen"))
        assertEquals("kotor_bay", areaKeyFor(" Kotor Bay "))
        assertEquals(areaKeyFor("kotor_bay"), areaKeyFor("Kotor-Bay"))
        assertEquals("old_town", areaKeyFor("Kotor"))
        assertEquals(GROUP_OTHER, areaKeyFor(null))
        assertEquals(GROUP_OTHER, areaKeyFor("  "))
        // A snapshot stored with the old distance bucket is shown under Other, not Worth the Drive.
        assertEquals(GROUP_OTHER, areaKeyFor("worth_the_drive"))
        // Idempotent: stored keys survive a second pass.
        assertEquals("kotor_bay", areaKeyFor(areaKeyFor("Kotor Bay")))
    }

    @Test fun distancesOnlyInsideTheBay() = run(
        favorites = listOf(entry("cathedral", 1)), places = listOf(oldTown), location = GeoPoint(48.85, 2.35) // Paris
    ) { vm, _ ->
        assertFalse(vm.state.value.inRegion)
        assertNull(vm.state.value.groups.single().items.single().distanceKm)
    }

    @Test fun inTheBayDistancesShowAndTheNearestAreaComesFirst() = run(
        favorites = listOf(entry("cathedral", 1), entry("rocks", 2)), places = listOf(oldTown, perast), location = GeoPoint(42.4870, 18.6980)
    ) { vm, _ ->
        assertTrue(vm.state.value.inRegion)
        assertEquals("perast", vm.state.value.groups.first().key)
        assertNotNull(vm.state.value.groups.first().items.single().distanceKm)
    }

    @Test fun unpublishedItemIsUnavailableWithItsCachedName() = run(
        favorites = listOf(entry("gone", 1)), places = listOf(oldTown),
        snapshots = mapOf(FavoriteKey(FavoriteItemType.PLACE, "gone") to SavedSnapshot(mapOf("en" to "Church of Saint Luke"), "old_town"))
    ) { vm, _ ->
        val item = vm.state.value.groups.single().items.single()
        assertTrue(item.unavailable)
        assertEquals("Church of Saint Luke", item.snapshot?.name?.get("en"))
        assertEquals("old_town", vm.state.value.groups.single().key)
    }

    @Test fun offlineFetchFailureIsNeverShownAsUnavailable() = run(
        favorites = listOf(entry("cathedral", 1)), places = null,
        snapshots = mapOf(FavoriteKey(FavoriteItemType.PLACE, "cathedral") to SavedSnapshot(mapOf("en" to "Cathedral"), "old_town", "p.jpg"))
    ) { vm, _ ->
        val item = vm.state.value.groups.single().items.single()
        assertFalse(item.unavailable)
        assertEquals(SavedItemStatus.NOT_REFRESHED, item.status)
        assertEquals("p.jpg", item.snapshot?.photo)
        assertTrue(vm.state.value.refreshFailed)
    }

    @Test fun resolutionIsNotReportedCompleteBeforeContentLoads() = run(
        favorites = listOf(entry("cathedral", 1)), places = listOf(oldTown), placesDelayMs = 1_000
    ) { vm, _ ->
        assertTrue(vm.state.value.isLoading)
        assertEquals(SavedItemStatus.LOADING, vm.state.value.groups.single().items.single().status)
        advanceTimeBy(1_001)
        runCurrent()
        assertFalse(vm.state.value.isLoading)
        assertEquals(SavedItemStatus.RESOLVED, vm.state.value.groups.single().items.single().status)
        assertFalse(vm.state.value.refreshFailed)
    }

    @Test fun repeatedRemoveNeverReAdds() = run(
        favorites = listOf(entry("cathedral", 1)), places = listOf(oldTown)
    ) { vm, favorites ->
        val entry = favorites.favoritesFlow.value.single()
        vm.onAction(FavoritesAction.Remove(entry, "Cathedral"))
        vm.onAction(FavoritesAction.Remove(entry, "Cathedral"))
        runCurrent()
        assertTrue(favorites.favoritesFlow.value.isEmpty())
    }

    @Test fun undoPutsTheItemBackInTheSamePlace() = run(
        favorites = listOf(entry("cathedral", 1), entry("cathedral2", 2), entry("cathedral3", 3)),
        places = listOf(oldTown, place("cathedral2", "old_town", 42.4247, 18.7713), place("cathedral3", "old_town", 42.4248, 18.7714))
    ) { vm, favorites ->
        val removed = favorites.favoritesFlow.value.first { it.id == "cathedral2" }
        vm.onAction(FavoritesAction.Remove(removed, "Cathedral 2"))
        runCurrent()
        assertEquals(listOf("cathedral3", "cathedral"), vm.state.value.groups.single().items.map { it.entry.id })
        vm.onAction(FavoritesAction.Undo(removed))
        runCurrent()
        assertEquals(listOf("cathedral3", "cathedral2", "cathedral"), vm.state.value.groups.single().items.map { it.entry.id })
    }

    private fun run(
        favorites: List<FavoriteEntry>,
        places: List<Place>?,
        location: GeoPoint? = null,
        snapshots: Map<FavoriteKey, SavedSnapshot> = emptyMap(),
        placesDelayMs: Long = 0,
        assertions: TestScope.(FavoritesViewModel, FakeFavorites) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val favs = FakeFavorites(favorites)
        val vm = FavoritesViewModel(
            placeRepository = object : PlaceRepository {
                override suspend fun getActivePlaces(): Result<List<Place>> {
                    delay(placesDelayMs)
                    return places?.let { Result.success(it) } ?: Result.failure(IllegalStateException("offline"))
                }
                override suspend fun getMustSeePlaces() = Result.success(emptyList<Place>())
                override suspend fun getPlacesByCategory(category: PlaceCategory) = Result.success(emptyList<Place>())
                override suspend fun getPlaceById(id: String): Result<Place> = error("unused")
            },
            getActivities = GetActivitiesUseCase(object : ExperienceRepository {
                override suspend fun getActivities() = Result.success(emptyList<Experience>())
                override suspend fun getActivityById(id: String): Result<Experience> = error("unused")
            }),
            favoritesRepository = favs,
            userLocationProvider = object : UserLocationProvider {
                override fun hasPermission() = location != null
                override suspend fun getCurrentLocation() = location
            },
            snapshots = object : SavedSnapshotRepository {
                val store = snapshots.toMutableMap()
                override fun get(key: FavoriteKey) = store[key]
                override fun putAll(snapshots: Map<FavoriteKey, SavedSnapshot>) { store.putAll(snapshots) }
            },
            navigator = RecordingNavigator(),
            languageSheet = LanguageSheetHost()
        )
        try {
            runCurrent()
            assertions(vm, favs)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    private fun entry(id: String, time: Long) = FavoriteEntry(FavoriteItemType.PLACE, id, time)

    private fun place(id: String, zone: String, lat: Double, lng: Double) = Place(
        id = id, cityId = "kotor", name = mapOf("en" to id), description = emptyMap(), longDescription = emptyMap(),
        localsTip = emptyMap(), category = PlaceCategory.SEE_AND_VISIT, subCategory = null, priceIndicator = null,
        latitude = lat, longitude = lng, zone = zone, tags = emptyList(), bestTime = BestTime.ANYTIME,
        estimatedDuration = null, openingHours = null, menuUrl = null, bookingPhone = null, photos = emptyList(),
        audioFile = null, audioAccessTier = "free", detailAccessTier = "free", isMustSee = false, isActive = true, sortOrder = 0
    )

    class FakeFavorites(initial: List<FavoriteEntry>) : FavoritesRepository {
        private val flow = MutableStateFlow(initial)
        override val favoritesFlow: StateFlow<List<FavoriteEntry>> = flow
        override fun isFavorited(type: FavoriteItemType, id: String) = flow.value.any { it.type == type && it.id == id }
        override fun save(type: FavoriteItemType, id: String): FavoriteEntry =
            flow.value.firstOrNull { it.type == type && it.id == id } ?: FavoriteEntry(type, id, 99).also { flow.value = flow.value + it }
        override fun remove(type: FavoriteItemType, id: String): FavoriteEntry? =
            flow.value.firstOrNull { it.type == type && it.id == id }?.also { e -> flow.value = flow.value - e }
        override fun restore(entry: FavoriteEntry) { if (!isFavorited(entry.type, entry.id)) flow.value = flow.value + entry }
        override fun getFavoriteKeySet() = flow.value.map { FavoriteKey(it.type, it.id) }.toSet()
    }
}
