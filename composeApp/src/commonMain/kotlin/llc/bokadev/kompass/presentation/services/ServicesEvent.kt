package llc.bokadev.kompass.presentation.services

import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class ServicesEvent : BaseEvent {
    data object NavigateBack : ServicesEvent()
    data object OnRetryClick : ServicesEvent()
    data class OnServiceClick(val id: String) : ServicesEvent()
}
