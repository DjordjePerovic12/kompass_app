package llc.bokadev.kompass.presentation.services.detail

import llc.bokadev.kompass.presentation.base.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed class ServiceDetailEvent : BaseEvent {
    data object NavigateBack : ServiceDetailEvent()
    data object OnRetryClick : ServiceDetailEvent()
    data object OnWebsiteClick : ServiceDetailEvent()
    data object OnMapsClick : ServiceDetailEvent()

    /** Emitted by the ViewModel on eventFlow: hands off to the browser or a maps app. */
    data class OpenExternal(val launch: ExternalLaunch) : ServiceDetailEvent()
}
