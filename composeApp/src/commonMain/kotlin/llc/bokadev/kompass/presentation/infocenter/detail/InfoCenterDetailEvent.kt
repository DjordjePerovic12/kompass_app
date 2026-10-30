package llc.bokadev.kompass.presentation.infocenter.detail

import llc.bokadev.kompass.presentation.base.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed class InfoCenterDetailEvent : BaseEvent {
    data object NavigateBack : InfoCenterDetailEvent()
    data object OnRetryClick : InfoCenterDetailEvent()
    data object OnSourceClick : InfoCenterDetailEvent()

    /** Emitted by the ViewModel on eventFlow: hands off to the browser. */
    data class OpenExternal(val launch: ExternalLaunch) : InfoCenterDetailEvent()
}
