package llc.bokadev.kompass.presentation.screens.services

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface ServicesAction : BaseAction {
    data object Retry : ServicesAction
    data object Back : ServicesAction
    data class OpenService(val id: String) : ServicesAction
}
