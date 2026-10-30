package llc.bokadev.kompass.presentation.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.states_couldnt_load_events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.NavType
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.model.EventFilterKind
import llc.bokadev.kompass.domain.repository.EventRepository
import llc.bokadev.kompass.domain.usecase.GetEventFiltersUseCase
import llc.bokadev.kompass.domain.usecase.GetEventsUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.testutil.FakeNavigator
import llc.bokadev.kompass.testutil.NoOpAnalytics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The recovery state contract (empty / filtered empty / failed / refresh failed / retry) for Events. */
@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    private val concert = event("concert", "music")
    private val match = event("match", "sport")

    @Test
    fun eventsLoadEvenWhenFiltersFail() = events(filters = null) { vm, _, _ ->
        assertEquals(listOf("concert", "match"), vm.eventsState.value.events.map { it.id })
        assertTrue(vm.eventsState.value.dateFilters.isEmpty())
        assertFalse(vm.eventsState.value.refreshFailed)
        assertNull(vm.eventsState.value.error)
    }

    @Test
    fun failedRetryKeepsTheEventsAndSaysSo() = events { vm, repo, _ ->
        repo.fail = true
        vm.onEvent(EventsEvent.OnRetryClick)
        runCurrent()
        assertTrue(vm.eventsState.value.refreshFailed)
        assertNull(vm.eventsState.value.error, "older events stay; it's a refresh failure, not an error page")
        assertEquals(2, vm.eventsState.value.events.size)
        assertFalse(vm.eventsState.value.isLoading, "retry with content never blanks the page")

        repo.fail = false
        vm.onEvent(EventsEvent.OnRetryClick)
        runCurrent()
        assertFalse(vm.eventsState.value.refreshFailed)
    }

    @Test
    fun firstLoadFailureIsALocalizedErrorWithNoEvents() = events(fail = true) { vm, _, _ ->
        assertNotNull(vm.eventsState.value.error)
        assertEquals(BaseError.InlineError(Res.string.states_couldnt_load_events), vm.eventsState.value.error)
        assertTrue(vm.eventsState.value.events.isEmpty())
    }

    @Test
    fun openingAnEventNavigatesThroughTheNavigator() = events(all = listOf(concert), filters = null) { vm, _, navigator ->
        vm.onEvent(EventsEvent.OnEventClick("concert"))
        vm.onEvent(EventsEvent.NavigateBack)
        runCurrent()
        assertEquals(listOf<Screen>(Screen.EventDetailScreen("concert")), navigator.destinations)
        assertEquals(NavType.NavigateUp, navigator.recorded.last())
    }

    @Test
    fun aSlowOlderFilterResponseCannotOverwriteANewerOne() = events { vm, repo, _ ->
        repo.delayFor = mapOf("sport" to 2_000L)
        vm.onEvent(EventsEvent.OnTypeFilterClick("sport"))
        runCurrent()
        assertTrue(vm.eventsState.value.isRefreshing)
        assertTrue(vm.eventsState.value.events.isEmpty(), "the previous filter's events aren't shown as this one's")
        vm.onEvent(EventsEvent.OnTypeFilterClick("music"))
        advanceUntilIdle()
        assertEquals(listOf("concert"), vm.eventsState.value.events.map { it.id })
    }

    @Test
    fun filteredEmptyCanBeReset() = events { vm, _, _ ->
        vm.onEvent(EventsEvent.OnDateFilterClick("today"))
        runCurrent()
        assertTrue(vm.eventsState.value.isFiltered)
        vm.onEvent(EventsEvent.OnResetFiltersClick)
        runCurrent()
        assertFalse(vm.eventsState.value.isFiltered)
        assertEquals(2, vm.eventsState.value.events.size)
    }

    private class FakeEvents(val all: List<Event>, val filters: List<EventFilter>?, var fail: Boolean) : EventRepository {
        var delayFor: Map<String, Long> = emptyMap()
        override suspend fun getEvents(dateFilter: String, eventType: String): Resource<List<Event>, NetworkError, String?> {
            delay(delayFor[eventType] ?: 0)
            if (fail) return Resource.Error(NetworkError.UNKNOWN, "offline")
            return Resource.Success(all.filter { (eventType == "all" || it.category == eventType) && dateFilter != "today" })
        }
        override suspend fun getEventFilters() = filters?.let { Resource.Success(it) } ?: Resource.Error(NetworkError.UNKNOWN, "no filters")
        override suspend fun getUpcomingEvents() = Resource.Success(all)
        override suspend fun getEventById(id: String): Resource<Event, NetworkError, String?> = error("unused")
    }

    /** Subscribes to the state (which starts the first load) and runs it before [assertions]. */
    private fun events(
        all: List<Event> = listOf(concert, match),
        filters: List<EventFilter>? = listOf(
            filter("all_dates", EventFilterKind.DATE, "all"), filter("today", EventFilterKind.DATE, "today"),
            filter("all_types", EventFilterKind.TYPE, "all"), filter("music", EventFilterKind.TYPE, "music"),
            filter("sport", EventFilterKind.TYPE, "sport")
        ),
        fail: Boolean = false,
        assertions: suspend TestScope.(EventsViewModel, FakeEvents, FakeNavigator) -> Unit
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeEvents(all, filters, fail)
        val navigator = FakeNavigator()
        val vm = EventsViewModel(GetEventsUseCase(repo), GetEventFiltersUseCase(repo), SavedStateHandle(), navigator, NoOpAnalytics())
        try {
            backgroundScope.launch { vm.eventsState.collect {} }
            runCurrent()
            assertions(vm, repo, navigator)
        } finally {
            vm.viewModelScope.cancel()
            advanceUntilIdle()
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
