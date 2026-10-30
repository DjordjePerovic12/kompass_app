package llc.bokadev.kompass.presentation.screens.infocenter

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface InfoCenterAction : BaseAction {
    data object Retry : InfoCenterAction
    data object Back : InfoCenterAction
    data class OpenNotice(val id: String) : InfoCenterAction
}
