package llc.bokadev.kompass.presentation.screens.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import llc.bokadev.kompass.core.util.appStringsFor
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.domain.repository.*
import llc.bokadev.kompass.domain.usecase.*
import llc.bokadev.kompass.presentation.screens.infocenter.InfoCenterAction
import llc.bokadev.kompass.presentation.screens.infocenter.InfoCenterViewModel
import llc.bokadev.kompass.presentation.screens.services.ServicesAction
import llc.bokadev.kompass.testutil.NoOpAnalytics
import llc.bokadev.kompass.testutil.RecordingNavigator
import llc.bokadev.kompass.presentation.screens.services.ServicesViewModel

/** The pilot state contract (empty / filtered empty / failed / refresh failed / retry) for Events, Local updates and Services. */
@OptIn(ExperimentalCoroutinesApi::class)
class RecoveryStatesTest {

    private val concert = event("concert", "music")
    private val match = event("match", "sport")

    @Test fun eventsLoadEvenWhenFiltersFail() = events(filters = null) { vm, _ ->
        assertEquals(listOf("concert", "match"), vm.state.value.events.map { it.id })
        assertTrue(vm.state.value.dateFilters.isEmpty())
        assertFalse(vm.state.value.refreshFailed)
        assertNull(vm.state.value.error)
    }

    @Test fun failedRetryKeepsTheEventsAndSaysSo() = events() { vm, repo ->
        repo.fail = true
        vm.onAction(EventsAction.Retry)
        runCurrent()
        assertTrue(vm.state.value.refreshFailed)
        assertNull(vm.state.value.error, "older events stay; it's a refresh failure, not an error page")
        assertEquals(2, vm.state.value.events.size)
        assertFalse(vm.state.value.isLoading, "retry with content never blanks the page")

        repo.fail = false
        vm.onAction(EventsAction.Retry)
        runCurrent()
        assertFalse(vm.state.value.refreshFailed)
    }

    @Test fun firstLoadFailureIsALocalizedErrorWithNoEvents() = events(fail = true) { vm, _ ->
        assertNotNull(vm.state.value.error)
        assertEquals("Couldn't load events.", vm.state.value.error?.resolve(appStringsFor("en")))
        assertTrue(vm.state.value.events.isEmpty())
    }

    @Test fun openingAnEventNavigatesThroughTheNavigator() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val navigator = RecordingNavigator()
        val repo = FakeEvents(listOf(concert), null, false)
        val vm = EventsViewModel(GetEventsUseCase(repo), GetEventFiltersUseCase(repo), SavedStateHandle(), navigator, NoOpAnalytics())
        try {
            runCurrent()
            vm.onAction(EventsAction.OpenEvent("concert"))
            vm.onAction(EventsAction.Back)
            assertEquals(listOf<llc.bokadev.kompass.presentation.navigation.Route>(llc.bokadev.kompass.presentation.navigation.Route.EventDetail("concert")), navigator.routes)
            assertEquals(llc.bokadev.kompass.core.presentation.mvi.NavigationCommand.NavigateUp, navigator.recorded.last())
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test fun aSlowOlderFilterResponseCannotOverwriteANewerOne() = events() { vm, repo ->
        repo.delayFor = mapOf("sport" to 2_000L)
        vm.onAction(EventsAction.SelectTypeFilter("sport"))
        runCurrent()
        assertTrue(vm.state.value.isRefreshing)
        assertTrue(vm.state.value.events.isEmpty(), "the previous filter's events aren't shown as this one's")
        vm.onAction(EventsAction.SelectTypeFilter("music"))
        advanceUntilIdle()
        assertEquals(listOf("concert"), vm.state.value.events.map { it.id })
    }

    @Test fun filteredEmptyCanBeReset() = events() { vm, _ ->
        vm.onAction(EventsAction.SelectDateFilter("today"))
        runCurrent()
        assertTrue(vm.state.value.isFiltered)
        vm.onAction(EventsAction.ResetFilters)
        runCurrent()
        assertFalse(vm.state.value.isFiltered)
        assertEquals(2, vm.state.value.events.size)
    }

    @Test fun noticesKeepContentOnFailedRetryAndRecover() = scoped {
        var fail = false
        val vm = InfoCenterViewModel(GetCurrentInfoNoticesUseCase(object : InfoCenterRepository {
            override suspend fun getCurrentNotices() =
                if (fail) Result.failure(IllegalStateException("x")) else Result.success(listOf(InfoNotice("n", "kotor", mapOf("en" to "Road closed"), emptyMap())))
            override suspend fun getNoticeById(id: String): Result<InfoNotice> = error("unused")
        }), RecordingNavigator(), NoOpAnalytics())
        runCurrent()
        fail = true
        vm.onAction(InfoCenterAction.Retry)
        runCurrent()
        assertTrue(vm.state.value.refreshFailed)
        assertEquals(1, vm.state.value.notices.size)
        assertNull(vm.state.value.error)
        vm
    }

    @Test fun servicesFailureWithoutDataIsAnErrorNotAnEmptyList() = scoped {
        var fail = true
        val vm = ServicesViewModel(GetServicesUseCase(object : ServiceRepository {
            override suspend fun getServices() =
                if (fail) Result.failure(IllegalStateException("x")) else Result.success(emptyList<Service>())
            override suspend fun getServiceById(id: String): Result<Service> = error("unused")
        }), RecordingNavigator(), NoOpAnalytics())
        runCurrent()
        assertNotNull(vm.state.value.error)
        fail = false
        vm.onAction(ServicesAction.Retry)
        runCurrent()
        assertNull(vm.state.value.error, "a successful empty result is 'none listed', not an error")
        assertTrue(vm.state.value.services.isEmpty())
        vm
    }

    private class FakeEvents(val all: List<Event>, val filters: List<EventFilter>?, var fail: Boolean) : EventRepository {
        var delayFor: Map<String, Long> = emptyMap()
        override suspend fun getEvents(dateFilter: String, eventType: String): Result<List<Event>> {
            delay(delayFor[eventType] ?: 0)
            if (fail) return Result.failure(IllegalStateException("offline"))
            return Result.success(all.filter { (eventType == "all" || it.category == eventType) && dateFilter != "today" })
        }
        override suspend fun getEventFilters() = filters?.let { Result.success(it) } ?: Result.failure(IllegalStateException("no filters"))
        override suspend fun getUpcomingEvents() = Result.success(all)
        override suspend fun getEventById(id: String): Result<Event> = error("unused")
    }

    private fun events(
        filters: List<EventFilter>? = listOf(
            filter("all_dates", EventFilterKind.DATE, "all"), filter("today", EventFilterKind.DATE, "today"),
            filter("all_types", EventFilterKind.TYPE, "all"), filter("music", EventFilterKind.TYPE, "music"),
            filter("sport", EventFilterKind.TYPE, "sport")
        ),
        fail: Boolean = false,
        assertions: TestScope.(EventsViewModel, FakeEvents) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeEvents(listOf(concert, match), filters, fail)
        val vm = EventsViewModel(GetEventsUseCase(repo), GetEventFiltersUseCase(repo), SavedStateHandle(), RecordingNavigator(), NoOpAnalytics())
        try {
            runCurrent()
            assertions(vm, repo)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    private fun scoped(block: TestScope.() -> androidx.lifecycle.ViewModel) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block().viewModelScope.cancel()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun filter(key: String, kind: EventFilterKind, value: String) =
        EventFilter(id = key, key = key, kind = kind, label = mapOf("en" to key), value = value, sortOrder = 0, isActive = true)

    private fun event(id: String, category: String) = Event(
        id = id, name = mapOf("en" to id), description = emptyMap(), venue = emptyMap(), latitude = 0.0, longitude = 0.0,
        category = category, startTime = "2026-10-08T18:00:00Z", endTime = null, price = null, ticketUrl = null,
        isRecurring = false, photos = emptyList()
    )
}
