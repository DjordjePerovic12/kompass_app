package llc.bokadev.kompass.presentation.placedetail

import llc.bokadev.kompass.presentation.base.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed class PlaceDetailEvent : BaseEvent {
    data object NavigateBack : PlaceDetailEvent()
    data object OnRetryClick : PlaceDetailEvent()

    /** [saved] is the state the visitor asked for (from what they saw), never a blind flip. */
    data class OnSavedChange(val saved: Boolean) : PlaceDetailEvent()
    data object OnMapsClick : PlaceDetailEvent()
    data class OnMenuClick(val url: String) : PlaceDetailEvent()
    data class OnCallClick(val phone: String) : PlaceDetailEvent()
    data object OnUserScrolled : PlaceDetailEvent()

    /** Emitted by the ViewModel on eventFlow: hands off to the browser, dialer or a maps app. */
    data class OpenExternal(val launch: ExternalLaunch) : PlaceDetailEvent()
}
