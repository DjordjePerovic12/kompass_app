package llc.bokadev.kompass.presentation.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.states_couldnt_load_events
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.model.EventFilterKind
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.usecase.GetEventFiltersUseCase
import llc.bokadev.kompass.domain.usecase.GetEventsUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel

class EventsViewModel(
    private val getEvents: GetEventsUseCase,
    private val getEventFilters: GetEventFiltersUseCase,
    private val savedState: SavedStateHandle,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<EventsState, EventsEvent>() {

    override val initialViewState = EventsState(
        selectedDateFilter = savedState[KEY_DATE_FILTER] ?: ALL_DATES,
        selectedTypeFilter = savedState[KEY_TYPE_FILTER] ?: ALL_TYPES
    )
    private var hasLoadedInitialData = false

    /** One request at a time: a newer filter or retry cancels the older one, so it can't overwrite it. */
    private var loadJob: Job? = null

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val eventsState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                analytics.trackScreenView("events")
                loadAll()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: EventsEvent) {
        when (event) {
            EventsEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            EventsEvent.OnRetryClick -> loadAll()
            is EventsEvent.OnDateFilterClick -> {
                savedState[KEY_DATE_FILTER] = event.key
                state.update { it.copy(selectedDateFilter = event.key) }
                reloadForFilters()
            }

            is EventsEvent.OnTypeFilterClick -> {
                savedState[KEY_TYPE_FILTER] = event.key
                state.update { it.copy(selectedTypeFilter = event.key) }
                reloadForFilters()
            }

            EventsEvent.OnResetFiltersClick -> {
                savedState[KEY_DATE_FILTER] = ALL_DATES
                savedState[KEY_TYPE_FILTER] = ALL_TYPES
                state.update {
                    it.copy(
                        selectedDateFilter = resolveSelectedKey(ALL_DATES, it.dateFilters, ALL_DATES),
                        selectedTypeFilter = resolveSelectedKey(ALL_TYPES, it.typeFilters, ALL_TYPES)
                    )
                }
                reloadForFilters()
            }

            is EventsEvent.OnEventClick -> {
                analytics.trackEventView(event.id, contentOrigin = "events_screen")
                viewModelScope.launch {
                    navigator.navigateTo(Screen.EventDetailScreen(event.id))
                }
            }
        }
    }

    /** Filters and events. Existing events stay on screen while retrying. */
    private fun loadAll() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update {
                val hasContent = it.events.isNotEmpty() || it.dateFilters.isNotEmpty()
                it.copy(isLoading = !hasContent, isRefreshing = hasContent, error = null, refreshFailed = false)
            }
            // Missing filters only hide the filter rows; they never block the events themselves.
            getEventFilters().onSuccess { filters ->
                val dateFilters = filters.filter { it.kind == EventFilterKind.DATE }.sortedBy { it.sortOrder }
                val typeFilters = filters.filter { it.kind == EventFilterKind.TYPE }.sortedBy { it.sortOrder }
                state.update {
                    it.copy(
                        dateFilters = dateFilters,
                        typeFilters = typeFilters,
                        selectedDateFilter = resolveSelectedKey(it.selectedDateFilter, dateFilters, ALL_DATES),
                        selectedTypeFilter = resolveSelectedKey(it.selectedTypeFilter, typeFilters, ALL_TYPES)
                    )
                }
            }
            fetchEvents()
        }
    }

    /** The previous filter's events would be wrong here, so they're cleared while the new ones load. */
    private fun reloadForFilters() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(events = emptyList(), isRefreshing = true, error = null, refreshFailed = false) }
            fetchEvents()
        }
    }

    private suspend fun fetchEvents() {
        val current = state.value
        getEvents(dateFilter = current.selectedDateFilter, eventType = selectedTypeFilterValue())
            .onSuccess { events ->
                state.update { it.copy(isLoading = false, isRefreshing = false, error = null, refreshFailed = false, events = events) }
            }
            .onError { _, _ ->
                state.update {
                    val hasEvents = it.events.isNotEmpty()
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = if (hasEvents) null else BaseError.InlineError(Res.string.states_couldnt_load_events),
                        refreshFailed = hasEvents
                    )
                }
            }
    }

    private fun selectedTypeFilterValue(): String =
        state.value.typeFilters.firstOrNull { it.key == state.value.selectedTypeFilter }?.value ?: "all"

    private fun resolveSelectedKey(currentKey: String, filters: List<EventFilter>, fallbackKey: String): String = when {
        filters.isEmpty() -> currentKey
        filters.any { it.key == currentKey } -> currentKey
        filters.any { it.key == fallbackKey } -> fallbackKey
        else -> filters.first().key
    }

    private companion object {
        const val KEY_DATE_FILTER = "selectedDateFilter"
        const val KEY_TYPE_FILTER = "selectedTypeFilter"
    }
}
