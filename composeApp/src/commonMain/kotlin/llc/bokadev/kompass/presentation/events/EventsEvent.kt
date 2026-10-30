package llc.bokadev.kompass.presentation.events

import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class EventsEvent : BaseEvent {
    data object NavigateBack : EventsEvent()
    data object OnRetryClick : EventsEvent()
    data class OnDateFilterClick(val key: String) : EventsEvent()
    data class OnTypeFilterClick(val key: String) : EventsEvent()
    data object OnResetFiltersClick : EventsEvent()
    data class OnEventClick(val id: String) : EventsEvent()
}
