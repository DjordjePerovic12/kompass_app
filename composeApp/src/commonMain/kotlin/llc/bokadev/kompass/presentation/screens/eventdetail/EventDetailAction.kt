package llc.bokadev.kompass.presentation.screens.eventdetail

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface EventDetailAction : BaseAction {
    data object Retry : EventDetailAction
    data object Back : EventDetailAction
    data object OpenTickets : EventDetailAction
    data object OpenMaps : EventDetailAction
}
