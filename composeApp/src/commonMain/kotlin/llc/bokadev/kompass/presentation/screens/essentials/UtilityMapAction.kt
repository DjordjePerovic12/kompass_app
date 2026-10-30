package llc.bokadev.kompass.presentation.screens.essentials

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface UtilityMapAction : BaseAction {
    data class SelectUtility(val id: String) : UtilityMapAction
    /** Hands the point with this id (never a name) to the system maps app. */
    data class Navigate(val id: String) : UtilityMapAction
    data object Retry : UtilityMapAction
    data object Back : UtilityMapAction
}
