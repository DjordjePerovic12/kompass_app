package llc.bokadev.kompass.presentation.screens.events

import llc.bokadev.kompass.core.presentation.base.BaseEvent

sealed interface EventsEvent : BaseEvent {
    data object Retry : EventsEvent
    data class SelectDateFilter(val value: String) : EventsEvent
    data class SelectTypeFilter(val value: String) : EventsEvent
}
