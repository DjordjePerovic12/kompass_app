package llc.bokadev.kompass.presentation.screens.categorylist

import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.testutil.RecordingNavigator
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*
import llc.bokadev.kompass.presentation.screens.favorites.FavoritesViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryListViewModelTest {

    private val cathedral = place("cathedral", "old_town", 42.4246, 18.7712, sortOrder = 2)
    private val nicholas = place("nicholas", "old_town", 42.4250, 18.7700, sortOrder = 1)
    private val rocks = place("rocks", "perast", 42.4868, 18.6982, sortOrder = 3)
    private val restaurant = place("konoba", "old_town", 42.4246, 18.7712, category = PlaceCategory.EAT_AND_DRINK)

    @Test fun savingNeverReordersTheList() {
        val items = placesFor(CategoryListKind.PLACES, listOf(cathedral, nicholas, rocks), location = null)
        assertEquals(listOf("nicholas", "cathedral", "rocks"), orderedFor(items, areaFilter = null).map { it.id })
    }

    @Test fun withoutLocationListUsesEditorialOrderAndNoDistances() {
        val items = orderedFor(placesFor(CategoryListKind.PLACES, listOf(cathedral, nicholas, rocks), location = null), null)
        assertEquals(listOf("nicholas", "cathedral", "rocks"), items.map { it.id })
        assertTrue(items.all { it.distanceKm == null })
    }

    @Test fun withLocationListIsByProximity() {
        val items = placesFor(CategoryListKind.PLACES, listOf(cathedral, nicholas, rocks), location = GeoPoint(42.4868, 18.6982))
        assertEquals("rocks", orderedFor(items, null).first().id)
    }

    @Test fun areaFilterNarrowsTheList() {
        val items = placesFor(CategoryListKind.PLACES, listOf(cathedral, nicholas, rocks), null)
        assertEquals(setOf("cathedral", "nicholas"), orderedFor(items, areaFilter = "old_town").map { it.id }.toSet())
        assertEquals(listOf("old_town", "perast"), areasOf(items))
    }

    @Test fun kindsPickTheirOwnData() {
        val all = listOf(cathedral, restaurant, rocks)
        assertEquals(listOf("konoba"), placesFor(CategoryListKind.EAT_AND_DRINK, all, null).map { it.id })
    }

    @Test fun noListIsBuiltFromDistanceAlone() {
        // Worth the Drive is the editorial collection, not "further than 3.5 km".
        assertFalse(CategoryListKind.entries.any { it.name == "WORTH_THE_DRIVE" })
        val far = place("lovcen", "lovcen", 42.3990, 18.8390)
        assertEquals("lovcen", placesFor(CategoryListKind.PLACES, listOf(far), null).single().areaKey)
    }

    @Test fun saveIsImmediateInPlaceAndIdempotentAndUndoRestoresTheTimestamp() = withVm(listOf(fav("rocks", 7))) { vm, favorites, effects, snapshots ->
        val before = vm.state.value.items.map { it.id }
        val cathedralItem = vm.state.value.items.first { it.id == "cathedral" }

        vm.onAction(CategoryListAction.SetSaved(cathedralItem, saved = true))
        // Persisted synchronously: navigating Back right away can't lose it.
        assertTrue(favorites.isFavorited(FavoriteItemType.PLACE, "cathedral"))
        runCurrent()
        assertEquals(before, vm.state.value.items.map { it.id }, "saving keeps the item where it was")
        assertTrue(vm.state.value.isSaved(cathedralItem))
        assertIs<CategoryListEvent.Saved>(effects.last())
        // A snapshot is remembered at save time, for Saved offline.
        assertEquals(mapOf("en" to "cathedral"), snapshots[FavoriteKey(FavoriteItemType.PLACE, "cathedral")]?.name)
        assertEquals("old_town", snapshots[FavoriteKey(FavoriteItemType.PLACE, "cathedral")]?.groupKey)

        // A duplicate "save" event (e.g. a double tap) neither re-saves nor removes.
        val count = effects.size
        vm.onAction(CategoryListAction.SetSaved(cathedralItem, saved = true))
        runCurrent()
        assertTrue(favorites.isFavorited(FavoriteItemType.PLACE, "cathedral"))
        assertEquals(count, effects.size)

        val rocksItem = vm.state.value.items.first { it.id == "rocks" }
        vm.onAction(CategoryListAction.SetSaved(rocksItem, saved = false))
        vm.onAction(CategoryListAction.SetSaved(rocksItem, saved = false)) // duplicate remove: no re-add
        runCurrent()
        assertFalse(favorites.isFavorited(FavoriteItemType.PLACE, "rocks"))
        val removed = effects.filterIsInstance<CategoryListEvent.Removed>().single()

        vm.onAction(CategoryListAction.Undo(removed.entry))
        runCurrent()
        assertEquals(7, favorites.favoritesFlow.value.first { it.id == "rocks" }.favoritedAtEpochMs)
        assertEquals(before, vm.state.value.items.map { it.id })
    }

    @Test fun openingARowNavigatesToItsDetail() = withVm(emptyList()) { vm, _, _, _ ->
        vm.onAction(CategoryListAction.Open(vm.state.value.items.first { it.id == "rocks" }))
        vm.onAction(CategoryListAction.OpenSearch)
        assertEquals(listOf<Screen>(Screen.PlaceDetailScreen("rocks"), Screen.SearchScreen), lastNavigator.routes)
    }

    private lateinit var lastNavigator: RecordingNavigator

    private fun withVm(
        initial: List<FavoriteEntry>,
        assertions: TestScope.(CategoryListViewModel, FavoritesViewModelTest.FakeFavorites, List<CategoryListEvent>, Map<FavoriteKey, SavedSnapshot>) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val favorites = FavoritesViewModelTest.FakeFavorites(initial)
        val snapshotStore = mutableMapOf<FavoriteKey, SavedSnapshot>()
        val navigator = RecordingNavigator().also { lastNavigator = it }
        val vm = CategoryListViewModel(
            placeRepository = object : PlaceRepository {
                override suspend fun getActivePlaces() = Resource.Success(listOf(cathedral, nicholas, rocks))
                override suspend fun getMustSeePlaces() = Resource.Success(emptyList<Place>())
                override suspend fun getPlacesByCategory(category: PlaceCategory) = Resource.Success(emptyList<Place>())
                override suspend fun getPlaceById(id: String): Resource<Place, NetworkError, String?> = error("unused")
            },
            getActivities = GetActivitiesUseCase(object : ExperienceRepository {
                override suspend fun getActivities() = Resource.Success(emptyList<Experience>())
                override suspend fun getActivityById(id: String): Resource<Experience, NetworkError, String?> = error("unused")
            }),
            favoritesRepository = favorites,
            userLocationProvider = object : UserLocationProvider {
                override fun hasPermission() = false
                override suspend fun getCurrentLocation(): GeoPoint? = null
            },
            savedState = SavedStateHandle(mapOf("kind" to "PLACES")),
            snapshots = object : SavedSnapshotRepository {
                override fun get(key: FavoriteKey) = snapshotStore[key]
                override fun putAll(snapshots: Map<FavoriteKey, SavedSnapshot>) { snapshotStore.putAll(snapshots) }
            },
            navigator = navigator
        )
        try {
            val effects = mutableListOf<CategoryListEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(effects) }
            runCurrent()
            assertions(vm, favorites, effects, snapshotStore)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    private fun fav(id: String, time: Long) = FavoriteEntry(FavoriteItemType.PLACE, id, time)

    private fun place(id: String, zone: String, lat: Double, lng: Double, sortOrder: Int = 0, category: PlaceCategory = PlaceCategory.SEE_AND_VISIT) = Place(
        id = id, cityId = "kotor", name = mapOf("en" to id), description = emptyMap(), longDescription = emptyMap(),
        localsTip = emptyMap(), category = category, subCategory = null, priceIndicator = null,
        latitude = lat, longitude = lng, zone = zone, tags = emptyList(), bestTime = BestTime.ANYTIME,
        estimatedDuration = null, openingHours = null, menuUrl = null, bookingPhone = null, photos = emptyList(),
        audioFile = null, audioAccessTier = "free", detailAccessTier = "free", isMustSee = false, isActive = true, sortOrder = sortOrder
    )
}
