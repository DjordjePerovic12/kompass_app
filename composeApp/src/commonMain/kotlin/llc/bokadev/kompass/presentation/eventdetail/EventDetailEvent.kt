package llc.bokadev.kompass.presentation.eventdetail

import llc.bokadev.kompass.presentation.base.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed class EventDetailEvent : BaseEvent {
    data object NavigateBack : EventDetailEvent()
    data object OnRetryClick : EventDetailEvent()
    data object OnTicketsClick : EventDetailEvent()
    data object OnMapsClick : EventDetailEvent()

    /** Emitted by the ViewModel on eventFlow: hands off to the browser or a maps app. */
    data class OpenExternal(val launch: ExternalLaunch) : EventDetailEvent()
}
