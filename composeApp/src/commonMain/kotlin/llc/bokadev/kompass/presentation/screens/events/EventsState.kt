package llc.bokadev.kompass.presentation.screens.events

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter

data class EventsState(
    /** First load only, before anything can be shown. */
    override val isLoading: Boolean = false,
    /** Nothing could be loaded at all (no events on screen). */
    override val error: UiText? = null,
    val events: List<Event> = emptyList(),
    val dateFilters: List<EventFilter> = emptyList(),
    val typeFilters: List<EventFilter> = emptyList(),
    val selectedDateFilter: String = ALL_DATES,
    val selectedTypeFilter: String = ALL_TYPES,
    /** A filter change or retry is in flight; the filters stay usable (inline progress only). */
    val isRefreshing: Boolean = false,
    /** The last refresh failed while older events are still shown. */
    val refreshFailed: Boolean = false
) : BaseViewState {
    val isFiltered: Boolean get() = selectedDateFilter != ALL_DATES || selectedTypeFilter != ALL_TYPES
}

const val ALL_DATES = "all_dates"
const val ALL_TYPES = "all_types"
