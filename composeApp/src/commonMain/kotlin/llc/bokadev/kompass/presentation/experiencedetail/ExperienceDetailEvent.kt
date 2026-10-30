package llc.bokadev.kompass.presentation.experiencedetail

import llc.bokadev.kompass.presentation.base.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed class ExperienceDetailEvent : BaseEvent {
    data object NavigateBack : ExperienceDetailEvent()
    data object OnRetryClick : ExperienceDetailEvent()

    /** [saved] is the state the visitor asked for (from what they saw), never a blind flip. */
    data class OnSavedChange(val saved: Boolean) : ExperienceDetailEvent()
    data object OnMapsClick : ExperienceDetailEvent()
    data class OnWebsiteClick(val url: String) : ExperienceDetailEvent()
    data object OnUserScrolled : ExperienceDetailEvent()

    /** Emitted by the ViewModel on eventFlow: hands off to the browser or a maps app. */
    data class OpenExternal(val launch: ExternalLaunch) : ExperienceDetailEvent()
}
