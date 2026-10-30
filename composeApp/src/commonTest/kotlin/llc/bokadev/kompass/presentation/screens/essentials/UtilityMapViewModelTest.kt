package llc.bokadev.kompass.presentation.screens.essentials

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.UtilityRepository
import llc.bokadev.kompass.domain.usecase.GetUtilitiesUseCase
import llc.bokadev.kompass.presentation.shared.UtilityMapCameraMove
import llc.bokadev.kompass.presentation.shared.UtilityMapCameraTracker
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import llc.bokadev.kompass.testutil.NoOpAnalytics
import llc.bokadev.kompass.testutil.RecordingNavigator

@OptIn(ExperimentalCoroutinesApi::class)
class UtilityMapViewModelTest {

    private val oldTown = GeoPoint(42.4247, 18.7712)
    // Two ATMs with exactly the same name at different coordinates.
    private val euronetGate = atm("euronet-gate", "Euronet", 42.4250, 18.7710)
    private val euronetSquare = atm("euronet-square", "Euronet", 42.4240, 18.7720)
    // ~4 km away: outside the nearest set while the visitor is in Old Town.
    private val dobrota = atm("dobrota", "Dobrota ATM", 42.4560, 18.7640)

    @Test fun identicalNamesSelectIndependentlyById() = run(listOf(euronetGate, euronetSquare)) { vm, _, _ ->
        vm.onAction(UtilityMapAction.SelectUtility("euronet-square"))
        val state = vm.state.value
        val selected = state.items.single { it.utility.id == state.selectedUtilityId }
        assertEquals(GeoPoint(42.4240, 18.7720), selected.point)

        vm.onAction(UtilityMapAction.SelectUtility("euronet-gate"))
        assertEquals(GeoPoint(42.4250, 18.7710), vm.state.value.items.single { it.utility.id == vm.state.value.selectedUtilityId }.point)
    }

    @Test fun requestedPinIsSelectedEvenOutsideTheNearestSet() =
        run(listOf(euronetGate, dobrota), selectedId = "dobrota") { vm, _, _ ->
            assertEquals("dobrota", vm.state.value.selectedUtilityId)
            assertTrue(vm.state.value.items.any { it.utility.id == "dobrota" })
        }

    @Test fun locationUpdatesNeverReplaceTheSelection() = run(listOf(euronetGate, euronetSquare, dobrota)) { vm, location, scope ->
        vm.onAction(UtilityMapAction.SelectUtility("euronet-square"))
        // Walk to Dobrota: Old Town's ATMs leave the nearest set, but the selected one stays.
        location.point = GeoPoint(42.4560, 18.7640)
        scope.advanceTimeBy(5_100)
        scope.runCurrent()
        val state = vm.state.value
        assertEquals("euronet-square", state.selectedUtilityId)
        assertTrue(state.items.any { it.utility.id == "euronet-square" })
        assertFalse(state.selectionGone)
    }

    @Test fun selectionRemovedFromDataIsExplainedNotSwapped() {
        val repo = FakeRepo(Result.success(listOf(euronetGate, euronetSquare)))
        run(repo = repo) { vm, _, scope ->
            vm.onAction(UtilityMapAction.SelectUtility("euronet-square"))
            repo.result = Result.success(listOf(euronetGate))
            vm.onAction(UtilityMapAction.Retry)
            scope.runCurrent()
            assertNull(vm.state.value.selectedUtilityId)
            assertTrue(vm.state.value.selectionGone)
        }
    }

    @Test fun failedRetryKeepsTheLoadedPoints() {
        val repo = FakeRepo(Result.success(listOf(euronetGate, euronetSquare)))
        run(repo = repo) { vm, _, scope ->
            vm.onAction(UtilityMapAction.SelectUtility("euronet-square"))
            repo.result = Result.failure(IllegalStateException("offline"))
            vm.onAction(UtilityMapAction.Retry)
            scope.runCurrent()
            val state = vm.state.value
            assertTrue(state.refreshFailed)
            assertEquals(2, state.items.size)
            assertEquals("euronet-square", state.selectedUtilityId)
            assertNull(state.error, "points are still shown: a refresh failure, not an error page")
        }
    }

    @Test fun firstLoadFailureShowsNoPoints() =
        run(repo = FakeRepo(Result.failure(IllegalStateException("offline")))) { vm, _, _ ->
            assertNotNull(vm.state.value.error)
            assertTrue(vm.state.value.items.isEmpty())
            assertFalse(vm.state.value.isLoading)
        }

    @Test fun deniedLocationStillListsUtilitiesFromOldTown() =
        run(listOf(euronetGate, euronetSquare), permission = false) { vm, _, _ ->
            assertNull(vm.state.value.currentLocation)
            assertEquals(2, vm.state.value.items.size)
            assertNotNull(vm.state.value.selectedUtilityId)
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

    private class FakeRepo(var result: Result<List<Utility>>) : UtilityRepository {
        override suspend fun getUtilities() = result
    }

    private class FakeLocation(var point: GeoPoint?, val permission: Boolean) : UserLocationProvider {
        override fun hasPermission() = permission
        override suspend fun getCurrentLocation() = if (permission) point else null
    }

    private fun run(
        utilities: List<Utility> = emptyList(),
        selectedId: String? = null,
        permission: Boolean = true,
        repo: FakeRepo = FakeRepo(Result.success(utilities)),
        assertions: (UtilityMapViewModel, FakeLocation, TestScope) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val location = FakeLocation(oldTown, permission)
            val vm = UtilityMapViewModel(
                SavedStateHandle(mapOf("category" to UtilityCategory.ATM.name, "selectedId" to selectedId)),
                GetUtilitiesUseCase(repo),
                location,
                RecordingNavigator(),
                NoOpAnalytics()
            )
            runCurrent()
            try {
                assertions(vm, location, this)
            } finally {
                // The ViewModel refreshes location every 5s forever; stop it before Main is reset.
                vm.viewModelScope.cancel()
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
