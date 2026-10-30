package llc.bokadev.kompass.presentation.screens.infocenter

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface InfoCenterDetailAction : BaseAction {
    data object Retry : InfoCenterDetailAction
    data object Back : InfoCenterDetailAction
    data object OpenSource : InfoCenterDetailAction
}
