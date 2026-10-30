package llc.bokadev.kompass.presentation.screens.placedetail

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface PlaceDetailAction : BaseAction {
    data object Retry : PlaceDetailAction
    data object Back : PlaceDetailAction
    /** [saved] is the state the visitor asked for (from what they saw), never a blind flip. */
    data class SetSaved(val saved: Boolean) : PlaceDetailAction
    data object OpenMaps : PlaceDetailAction
    data class OpenMenu(val url: String) : PlaceDetailAction
    data class Call(val phone: String) : PlaceDetailAction
    data object UserScrolled : PlaceDetailAction
    /** The Listen control: play / pause / retry this screen's audio. Handled by the orchestrator (owns playback). */
    data object Listen : PlaceDetailAction
}
