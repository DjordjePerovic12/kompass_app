package llc.bokadev.kompass.presentation.screens.events

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface EventsAction : BaseAction {
    data object Retry : EventsAction
    data class SelectDateFilter(val key: String) : EventsAction
    data class SelectTypeFilter(val key: String) : EventsAction
    data object ResetFilters : EventsAction
    data class OpenEvent(val id: String) : EventsAction
    data object Back : EventsAction
}
