package llc.bokadev.kompass.presentation.screens.events

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.EventFilterKind
import llc.bokadev.kompass.domain.usecase.GetEventFiltersUseCase
import llc.bokadev.kompass.domain.usecase.GetEventsUseCase

class EventsViewModel(
    private val getEvents: GetEventsUseCase,
    private val getEventFilters: GetEventFiltersUseCase
) : BaseViewModel<EventsState, EventsEvent>() {

    override val initialState = EventsState()

    private var loadEventsJob: Job? = null

    init { loadInitialData() }

    override fun onIntent(event: EventsEvent) {
        when (event) {
            EventsEvent.Retry -> loadInitialData()
            is EventsEvent.SelectDateFilter -> {
                _state.update { it.copy(selectedDateFilter = event.value) }
                loadEvents()
            }
            is EventsEvent.SelectTypeFilter -> {
                _state.update { it.copy(selectedTypeFilter = event.value) }
                loadEvents()
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getEventFilters()
                .onSuccess { filters ->
                    val dateFilters = filters
                        .filter { filter -> filter.kind == EventFilterKind.DATE }
                        .sortedBy { filter -> filter.sortOrder }
                    val typeFilters = filters
                        .filter { filter -> filter.kind == EventFilterKind.TYPE }
                        .sortedBy { filter -> filter.sortOrder }

                    val selectedDateKey = resolveSelectedKey(
                        currentKey = state.value.selectedDateFilter,
                        filters = dateFilters,
                        fallbackKey = "all_dates"
                    )
                    val selectedTypeKey = resolveSelectedKey(
                        currentKey = state.value.selectedTypeFilter,
                        filters = typeFilters,
                        fallbackKey = "all_types"
                    )

                    _state.update {
                        it.copy(
                            dateFilters = dateFilters,
                            typeFilters = typeFilters,
                            selectedDateFilter = selectedDateKey,
                            selectedTypeFilter = selectedTypeKey
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                    return@launch
                }

            getEvents(
                dateFilter = state.value.selectedDateFilter,
                eventType = selectedTypeFilterValue()
            )
                .onSuccess { events ->
                    _state.update { it.copy(isLoading = false, events = events) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }

    private fun loadEvents() {
        loadEventsJob?.cancel()
        loadEventsJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getEvents(
                dateFilter = state.value.selectedDateFilter,
                eventType = selectedTypeFilterValue()
            )
                .onSuccess { events -> _state.update { it.copy(isLoading = false, events = events) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }

    private fun selectedTypeFilterValue(): String {
        return state.value.typeFilters
            .firstOrNull { it.key == state.value.selectedTypeFilter }
            ?.value
            ?: "all"
    }

    private fun resolveSelectedKey(
        currentKey: String,
        filters: List<llc.bokadev.kompass.domain.model.EventFilter>,
        fallbackKey: String
    ): String {
        return when {
            filters.any { it.key == currentKey } -> currentKey
            filters.any { it.key == fallbackKey } -> fallbackKey
            else -> filters.firstOrNull()?.key ?: currentKey
        }
    }
}
