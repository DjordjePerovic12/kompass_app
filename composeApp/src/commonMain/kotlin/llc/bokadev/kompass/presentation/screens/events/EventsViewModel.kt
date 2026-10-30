package llc.bokadev.kompass.presentation.screens.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.model.EventFilterKind
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.usecase.GetEventFiltersUseCase
import llc.bokadev.kompass.domain.usecase.GetEventsUseCase
import llc.bokadev.kompass.presentation.navigation.Route

class EventsViewModel(
    private val getEvents: GetEventsUseCase,
    private val getEventFilters: GetEventFiltersUseCase,
    private val savedState: SavedStateHandle,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<EventsState, EventsAction, Nothing>(
    EventsState(
        selectedDateFilter = savedState["selectedDateFilter"] ?: ALL_DATES,
        selectedTypeFilter = savedState["selectedTypeFilter"] ?: ALL_TYPES
    )
) {

    /** One request at a time: a newer filter or retry cancels the older one, so it can't overwrite it. */
    private var loadJob: Job? = null

    override suspend fun onFirstStart() {
        analytics.trackScreenView("events")
        loadAll()
    }

    override fun onAction(action: EventsAction) {
        when (action) {
            EventsAction.Retry -> loadAll()
            is EventsAction.SelectDateFilter -> {
                savedState["selectedDateFilter"] = action.key
                updateState { it.copy(selectedDateFilter = action.key) }
                reloadForFilters()
            }
            is EventsAction.SelectTypeFilter -> {
                savedState["selectedTypeFilter"] = action.key
                updateState { it.copy(selectedTypeFilter = action.key) }
                reloadForFilters()
            }
            EventsAction.ResetFilters -> {
                savedState["selectedDateFilter"] = ALL_DATES
                savedState["selectedTypeFilter"] = ALL_TYPES
                updateState {
                    it.copy(
                        selectedDateFilter = resolveSelectedKey(ALL_DATES, it.dateFilters, ALL_DATES),
                        selectedTypeFilter = resolveSelectedKey(ALL_TYPES, it.typeFilters, ALL_TYPES)
                    )
                }
                reloadForFilters()
            }
            is EventsAction.OpenEvent -> {
                analytics.trackEventView(action.id, contentOrigin = "events_screen")
                navigator.navigateTo(Route.EventDetail(action.id))
            }
            EventsAction.Back -> navigator.navigateUp()
        }
    }

    /** Filters and events. Existing events stay on screen while retrying. */
    private fun loadAll() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState {
                val hasContent = it.events.isNotEmpty() || it.dateFilters.isNotEmpty()
                it.copy(isLoading = !hasContent, isRefreshing = hasContent, error = null, refreshFailed = false)
            }
            // Missing filters only hide the filter rows; they never block the events themselves.
            getEventFilters().onSuccess { filters ->
                val dateFilters = filters.filter { it.kind == EventFilterKind.DATE }.sortedBy { it.sortOrder }
                val typeFilters = filters.filter { it.kind == EventFilterKind.TYPE }.sortedBy { it.sortOrder }
                updateState {
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
            updateState { it.copy(events = emptyList(), isRefreshing = true, error = null, refreshFailed = false) }
            fetchEvents()
        }
    }

    private suspend fun fetchEvents() {
        val current = state.value
        getEvents(dateFilter = current.selectedDateFilter, eventType = selectedTypeFilterValue())
            .onSuccess { events ->
                updateState { it.copy(isLoading = false, isRefreshing = false, error = null, refreshFailed = false, events = events) }
            }
            .onFailure {
                updateState {
                    val hasEvents = it.events.isNotEmpty()
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = if (hasEvents) null else LOAD_FAILED,
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
        val LOAD_FAILED = UiText { it.states.couldntLoadEvents }
    }
}
