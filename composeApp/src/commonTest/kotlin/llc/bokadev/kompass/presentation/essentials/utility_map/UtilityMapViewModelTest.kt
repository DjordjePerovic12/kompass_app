package llc.bokadev.kompass.presentation.essentials.utility_map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.UtilityRepository
import llc.bokadev.kompass.domain.usecase.GetUtilitiesUseCase
import llc.bokadev.kompass.presentation.shared.UtilityMapCameraMove
import llc.bokadev.kompass.presentation.shared.UtilityMapCameraTracker
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import llc.bokadev.kompass.testutil.NoOpAnalytics
import llc.bokadev.kompass.testutil.FakeNavigator
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import kotlinx.coroutines.flow.toList

@OptIn(ExperimentalCoroutinesApi::class)
class UtilityMapViewModelTest {

    private val oldTown = GeoPoint(42.4247, 18.7712)
    // Two ATMs with exactly the same name at different coordinates.
    private val euronetGate = atm("euronet-gate", "Euronet", 42.4250, 18.7710)
    private val euronetSquare = atm("euronet-square", "Euronet", 42.4240, 18.7720)
    // ~4 km away: outside the nearest set while the visitor is in Old Town.
    private val dobrota = atm("dobrota", "Dobrota ATM", 42.4560, 18.7640)

    @Test fun identicalNamesSelectIndependentlyById() = run(listOf(euronetGate, euronetSquare)) { vm, _, _, _ ->
        vm.onEvent(UtilityMapEvent.OnUtilitySelect("euronet-square"))
        val state = vm.viewStateFlow.value
        val selected = state.items.single { it.utility.id == state.selectedUtilityId }
        assertEquals(GeoPoint(42.4240, 18.7720), selected.point)

        vm.onEvent(UtilityMapEvent.OnUtilitySelect("euronet-gate"))
        assertEquals(GeoPoint(42.4250, 18.7710), vm.viewStateFlow.value.items.single { it.utility.id == vm.viewStateFlow.value.selectedUtilityId }.point)
    }

    @Test fun requestedPinIsSelectedEvenOutsideTheNearestSet() =
        run(listOf(euronetGate, dobrota), selectedId = "dobrota") { vm, _, _, _ ->
            assertEquals("dobrota", vm.viewStateFlow.value.selectedUtilityId)
            assertTrue(vm.viewStateFlow.value.items.any { it.utility.id == "dobrota" })
        }

    @Test fun locationUpdatesNeverReplaceTheSelection() = run(listOf(euronetGate, euronetSquare, dobrota)) { vm, location, scope, _ ->
        vm.onEvent(UtilityMapEvent.OnUtilitySelect("euronet-square"))
        // Walk to Dobrota: Old Town's ATMs leave the nearest set, but the selected one stays.
        location.point = GeoPoint(42.4560, 18.7640)
        scope.advanceTimeBy(5_100)
        scope.runCurrent()
        val state = vm.viewStateFlow.value
        assertEquals("euronet-square", state.selectedUtilityId)
        assertTrue(state.items.any { it.utility.id == "euronet-square" })
        assertFalse(state.selectionGone)
    }

    @Test fun selectionRemovedFromDataIsExplainedNotSwapped() {
        val repo = FakeRepo(Resource.Success(listOf(euronetGate, euronetSquare)))
        run(repo = repo) { vm, _, scope, _ ->
            vm.onEvent(UtilityMapEvent.OnUtilitySelect("euronet-square"))
            repo.result = Resource.Success(listOf(euronetGate))
            vm.onEvent(UtilityMapEvent.OnRetryClick)
            scope.runCurrent()
            assertNull(vm.viewStateFlow.value.selectedUtilityId)
            assertTrue(vm.viewStateFlow.value.selectionGone)
        }
    }

    @Test fun failedRetryKeepsTheLoadedPoints() {
        val repo = FakeRepo(Resource.Success(listOf(euronetGate, euronetSquare)))
        run(repo = repo) { vm, _, scope, _ ->
            vm.onEvent(UtilityMapEvent.OnUtilitySelect("euronet-square"))
            repo.result = Resource.Error(NetworkError.UNKNOWN, "offline")
            vm.onEvent(UtilityMapEvent.OnRetryClick)
            scope.runCurrent()
            val state = vm.viewStateFlow.value
            assertTrue(state.refreshFailed)
            assertEquals(2, state.items.size)
            assertEquals("euronet-square", state.selectedUtilityId)
            assertNull(state.error, "points are still shown: a refresh failure, not an error page")
        }
    }

    @Test fun firstLoadFailureShowsNoPoints() =
        run(repo = FakeRepo(Resource.Error(NetworkError.UNKNOWN, "offline"))) { vm, _, _, _ ->
            assertNotNull(vm.viewStateFlow.value.error)
            assertTrue(vm.viewStateFlow.value.items.isEmpty())
            assertFalse(vm.viewStateFlow.value.isLoading)
        }

    @Test fun deniedLocationStillListsUtilitiesFromOldTown() =
        run(listOf(euronetGate, euronetSquare), permission = false) { vm, _, _, _ ->
            assertNull(vm.viewStateFlow.value.currentLocation)
            assertEquals(2, vm.viewStateFlow.value.items.size)
            assertNotNull(vm.viewStateFlow.value.selectedUtilityId)
        }

    @Test fun navigateHandsTheSelectedPointToMapsAndBackNavigatesUp() =
        run(listOf(euronetGate, euronetSquare)) { vm, _, scope, navigator ->
            val events = mutableListOf<UtilityMapEvent>()
            scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { vm.eventFlow.toList(events) }
            vm.onEvent(UtilityMapEvent.OnNavigateClick("euronet-square"))
            vm.onEvent(UtilityMapEvent.NavigateBack)
            scope.runCurrent()
            assertEquals(listOf<UtilityMapEvent>(UtilityMapEvent.OpenExternal(ExternalLaunch.Maps(42.4240, 18.7720, "Euronet"))), events)
            assertEquals(listOf<NavType>(NavType.NavigateUp), navigator.recorded)
        }

    @Test fun cameraMovesOnlyForNewPinsOrNewSelection() {
        val tracker = UtilityMapCameraTracker()
        val pins = listOf(pin(euronetGate), pin(euronetSquare))
        val first = tracker.next(pins, "euronet-gate", oldTown)
        assertIs<UtilityMapCameraMove.Frame>(first)
        assertEquals(3, first.points.size, "a nearby user is framed with the pins")

        // A location update alone leaves the camera where the visitor put it.
        assertEquals(UtilityMapCameraMove.None, tracker.next(pins, "euronet-gate", GeoPoint(42.4249, 18.7713)))
        // Selecting another pin pans to exactly that pin.
        assertEquals(UtilityMapCameraMove.FocusOn(GeoPoint(42.4240, 18.7720)), tracker.next(pins, "euronet-square", oldTown))
        assertEquals(UtilityMapCameraMove.None, tracker.next(pins, "euronet-square", oldTown))
        // A different pin set reframes; a far-away user is left out of the frame.
        val reframed = tracker.next(listOf(pin(dobrota)), "dobrota", GeoPoint(37.77, -122.42))
        assertEquals(UtilityMapCameraMove.Frame(listOf(GeoPoint(42.4560, 18.7640))), reframed)
    }

    private class FakeRepo(var result: Resource<List<Utility>, NetworkError, String?>) : UtilityRepository {
        override suspend fun getUtilities() = result
    }

    @Test fun locationPollingRunsOnlyWhileTheStateIsCollected() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val location = FakeLocation(oldTown, permission = true)
            val vm = UtilityMapViewModel(
                SavedStateHandle(mapOf("category" to UtilityCategory.ATM.name)),
                GetUtilitiesUseCase(FakeRepo(Resource.Success(listOf(euronetGate)))),
                location,
                FakeNavigator(),
                NoOpAnalytics()
            )
            advanceTimeBy(20_000)
            runCurrent()
            assertEquals(0, location.calls, "no GPS reads before the screen subscribes")

            val collector = backgroundScope.launch { vm.utilityMapState.collect {} }
            runCurrent()
            val afterLoad = location.calls
            advanceTimeBy(10_100)
            runCurrent()
            assertEquals(afterLoad + 2, location.calls, "polls every 5 s while subscribed")

            collector.cancel()
            advanceTimeBy(5_100) // WhileSubscribed stop timeout
            runCurrent()
            val afterStop = location.calls
            advanceTimeBy(30_000)
            runCurrent()
            assertEquals(afterStop, location.calls, "polling stops once nobody collects")
            vm.viewModelScope.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeLocation(var point: GeoPoint?, val permission: Boolean) : UserLocationProvider {
        var calls = 0
        override fun hasPermission() = permission
        override suspend fun getCurrentLocation(): GeoPoint? {
            calls++
            return if (permission) point else null
        }
    }

    private fun run(
        utilities: List<Utility> = emptyList(),
        selectedId: String? = null,
        permission: Boolean = true,
        repo: FakeRepo = FakeRepo(Resource.Success(utilities)),
        assertions: suspend (UtilityMapViewModel, FakeLocation, TestScope, FakeNavigator) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val location = FakeLocation(oldTown, permission)
            val navigator = FakeNavigator()
            val vm = UtilityMapViewModel(
                SavedStateHandle(mapOf("category" to UtilityCategory.ATM.name, "selectedId" to selectedId)),
                GetUtilitiesUseCase(repo),
                location,
                navigator,
                NoOpAnalytics()
            )
            // Subscribing starts the first load.
            backgroundScope.launch { vm.utilityMapState.collect {} }
            runCurrent()
            try {
                assertions(vm, location, this, navigator)
            } finally {
                // The subscribed state (and its 5 s location polling) lives in viewModelScope; stop it before Main is reset.
                vm.viewModelScope.cancel()
                advanceUntilIdle()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun atm(id: String, name: String, lat: Double, lng: Double) =
        Utility(id = id, name = name, category = UtilityCategory.ATM, latitude = lat, longitude = lng, address = null, googlePlaceId = null)

    private fun pin(utility: Utility) =
        UtilityMapPin(utility.id, utility.name, GeoPoint(utility.latitude!!, utility.longitude!!))
}
