package llc.bokadev.kompass.presentation.infocenter

import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class InfoCenterEvent : BaseEvent {
    data object NavigateBack : InfoCenterEvent()
    data object OnRetryClick : InfoCenterEvent()
    data class OnNoticeClick(val id: String) : InfoCenterEvent()
}
