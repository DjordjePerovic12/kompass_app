package llc.bokadev.kompass.presentation.screens.events

import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter

data class EventsState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val events: List<Event> = emptyList(),
    val dateFilters: List<EventFilter> = emptyList(),
    val typeFilters: List<EventFilter> = emptyList(),
    val selectedDateFilter: String = "all_dates",
    val selectedTypeFilter: String = "all_types"
) : BaseState()
