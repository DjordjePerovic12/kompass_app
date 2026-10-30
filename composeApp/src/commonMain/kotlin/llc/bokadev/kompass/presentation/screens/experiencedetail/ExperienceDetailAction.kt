package llc.bokadev.kompass.presentation.screens.experiencedetail

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface ExperienceDetailAction : BaseAction {
    data object Retry : ExperienceDetailAction
    data object Back : ExperienceDetailAction
    /** [saved] is the state the visitor asked for (from what they saw), never a blind flip. */
    data class SetSaved(val saved: Boolean) : ExperienceDetailAction
    data object OpenMaps : ExperienceDetailAction
    data class OpenWebsite(val url: String) : ExperienceDetailAction
    data object UserScrolled : ExperienceDetailAction
    /** The Listen control: play / pause / retry this screen's audio. Handled by the orchestrator (owns playback). */
    data object Listen : ExperienceDetailAction
}
