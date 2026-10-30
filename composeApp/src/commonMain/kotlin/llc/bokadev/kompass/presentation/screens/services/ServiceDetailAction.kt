package llc.bokadev.kompass.presentation.screens.services

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface ServiceDetailAction : BaseAction {
    data object Retry : ServiceDetailAction
    data object Back : ServiceDetailAction
    data object OpenWebsite : ServiceDetailAction
    data object OpenMaps : ServiceDetailAction
}
