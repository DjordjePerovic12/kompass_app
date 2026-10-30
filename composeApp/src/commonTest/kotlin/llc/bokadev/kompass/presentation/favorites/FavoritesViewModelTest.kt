package llc.bokadev.kompass.presentation.favorites

import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*
import llc.bokadev.kompass.presentation.shared.GROUP_OTHER
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import llc.bokadev.kompass.presentation.shared.areaKeyFor
import llc.bokadev.kompass.testutil.FakeFavorites
import llc.bokadev.kompass.testutil.FakeNavigator

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val oldTown = place("cathedral", "old_town", 42.4246, 18.7712)
    private val perast = place("rocks", "perast", 42.4868, 18.6982)
    private val lovcen = place("lovcen", "lovcen", 42.3990, 18.8390) // ~7 km out, no curated zone

    @Test fun groupsFollowAreaOrderAndNewestFirst() = run(
        favorites = listOf(entry("rocks", 1), entry("cathedral", 2), entry("lovcen", 3), entry("cathedral2", 4)),
        places = listOf(oldTown, perast, lovcen, place("cathedral2", "kotor", 42.4247, 18.7713))
    ) { vm, _, _ ->
        val groups = vm.favoritesState.value.groups
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
        // Local letters survive; this also runs on iOS, where \p{L} regexes are unsupported.
        assertEquals("prčanj", areaKeyFor("Prčanj"))
        assertEquals("škaljari_bay", areaKeyFor("Škaljari – Bay"))
    }

    @Test fun distancesOnlyInsideTheBay() = run(
        favorites = listOf(entry("cathedral", 1)), places = listOf(oldTown), location = GeoPoint(48.85, 2.35) // Paris
    ) { vm, _, _ ->
        assertFalse(vm.favoritesState.value.inRegion)
        assertNull(vm.favoritesState.value.groups.single().items.single().distanceKm)
    }

    @Test fun inTheBayDistancesShowAndTheNearestAreaComesFirst() = run(
        favorites = listOf(entry("cathedral", 1), entry("rocks", 2)), places = listOf(oldTown, perast), location = GeoPoint(42.4870, 18.6980)
    ) { vm, _, _ ->
        assertTrue(vm.favoritesState.value.inRegion)
        assertEquals("perast", vm.favoritesState.value.groups.first().key)
        assertNotNull(vm.favoritesState.value.groups.first().items.single().distanceKm)
    }

    @Test fun unpublishedItemIsUnavailableWithItsCachedName() = run(
        favorites = listOf(entry("gone", 1)), places = listOf(oldTown),
        snapshots = mapOf(FavoriteKey(FavoriteItemType.PLACE, "gone") to SavedSnapshot(mapOf("en" to "Church of Saint Luke"), "old_town"))
    ) { vm, _, _ ->
        val item = vm.favoritesState.value.groups.single().items.single()
        assertTrue(item.unavailable)
        assertEquals("Church of Saint Luke", item.snapshot?.name?.get("en"))
        assertEquals("old_town", vm.favoritesState.value.groups.single().key)
    }

    @Test fun offlineFetchFailureIsNeverShownAsUnavailable() = run(
        favorites = listOf(entry("cathedral", 1)), places = null,
        snapshots = mapOf(FavoriteKey(FavoriteItemType.PLACE, "cathedral") to SavedSnapshot(mapOf("en" to "Cathedral"), "old_town", "p.jpg"))
    ) { vm, _, _ ->
        val item = vm.favoritesState.value.groups.single().items.single()
        assertFalse(item.unavailable)
        assertEquals(SavedItemStatus.NOT_REFRESHED, item.status)
        assertEquals("p.jpg", item.snapshot?.photo)
        assertTrue(vm.favoritesState.value.refreshFailed)
    }

    @Test fun resolutionIsNotReportedCompleteBeforeContentLoads() = run(
        favorites = listOf(entry("cathedral", 1)), places = listOf(oldTown), placesDelayMs = 1_000
    ) { vm, _, _ ->
        assertTrue(vm.favoritesState.value.isLoading)
        assertEquals(SavedItemStatus.LOADING, vm.favoritesState.value.groups.single().items.single().status)
        advanceTimeBy(1_001)
        runCurrent()
        assertFalse(vm.favoritesState.value.isLoading)
        assertEquals(SavedItemStatus.RESOLVED, vm.favoritesState.value.groups.single().items.single().status)
        assertFalse(vm.favoritesState.value.refreshFailed)
    }

    @Test fun repeatedRemoveNeverReAdds() = run(
        favorites = listOf(entry("cathedral", 1)), places = listOf(oldTown)
    ) { vm, favorites, events ->
        val entry = favorites.favoritesFlow.value.single()
        vm.onEvent(FavoritesEvent.OnRemoveClick(entry, "Cathedral"))
        vm.onEvent(FavoritesEvent.OnRemoveClick(entry, "Cathedral"))
        runCurrent()
        assertTrue(favorites.favoritesFlow.value.isEmpty())
        // Only the real removal confirms.
        assertEquals(listOf<FavoritesEvent>(FavoritesEvent.ShowRemoved(entry, "Cathedral")), events)
    }

    @Test fun undoPutsTheItemBackInTheSamePlace() = run(
        favorites = listOf(entry("cathedral", 1), entry("cathedral2", 2), entry("cathedral3", 3)),
        places = listOf(oldTown, place("cathedral2", "old_town", 42.4247, 18.7713), place("cathedral3", "old_town", 42.4248, 18.7714))
    ) { vm, favorites, _ ->
        val removed = favorites.favoritesFlow.value.first { it.id == "cathedral2" }
        vm.onEvent(FavoritesEvent.OnRemoveClick(removed, "Cathedral 2"))
        runCurrent()
        assertEquals(listOf("cathedral3", "cathedral"), vm.favoritesState.value.groups.single().items.map { it.entry.id })
        vm.onEvent(FavoritesEvent.OnUndoClick(removed))
        runCurrent()
        assertEquals(listOf("cathedral3", "cathedral2", "cathedral"), vm.favoritesState.value.groups.single().items.map { it.entry.id })
    }

    @Test fun openingAnItemAndExploringNavigate() = run(
        favorites = listOf(entry("cathedral", 1)), places = listOf(oldTown)
    ) { vm, _, _ ->
        vm.onEvent(FavoritesEvent.OnItemClick(FavoriteItemType.PLACE, "cathedral"))
        vm.onEvent(FavoritesEvent.OnItemClick(FavoriteItemType.ACTIVITY, "kayak"))
        vm.onEvent(FavoritesEvent.OnExploreKotorClick)
        runCurrent()
        assertEquals(listOf<Screen>(Screen.PlaceDetailScreen("cathedral"), Screen.ExperienceDetailScreen("kayak")), lastNavigator.destinations)
        assertEquals(NavType.SwitchTab(BottomTab.Explore), lastNavigator.recorded.last())
    }

    private lateinit var lastNavigator: FakeNavigator

    private fun run(
        favorites: List<FavoriteEntry>,
        places: List<Place>?,
        location: GeoPoint? = null,
        snapshots: Map<FavoriteKey, SavedSnapshot> = emptyMap(),
        placesDelayMs: Long = 0,
        assertions: TestScope.(FavoritesViewModel, FakeFavorites, List<FavoritesEvent>) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val favs = FakeFavorites(favorites)
        val vm = FavoritesViewModel(
            placeRepository = object : PlaceRepository {
                override suspend fun getActivePlaces(): Resource<List<Place>, NetworkError, String?> {
                    delay(placesDelayMs)
                    return places?.let { Resource.Success(it) } ?: Resource.Error(NetworkError.UNKNOWN, "offline")
                }
                override suspend fun getMustSeePlaces() = Resource.Success(emptyList<Place>())
                override suspend fun getPlaceById(id: String): Resource<Place, NetworkError, String?> = error("unused")
            },
            getActivities = GetActivitiesUseCase(object : ExperienceRepository {
                override suspend fun getActivities() = Resource.Success(emptyList<Experience>())
                override suspend fun getActivityById(id: String): Resource<Experience, NetworkError, String?> = error("unused")
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
            navigator = FakeNavigator().also { lastNavigator = it },
            languageSheet = LanguageSheetHost()
        )
        try {
            val events = mutableListOf<FavoritesEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.eventFlow.toList(events) }
            // Saved loads when the screen first collects its state.
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.favoritesState.collect {} }
            runCurrent()
            assertions(vm, favs, events)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
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
}
