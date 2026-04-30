package llc.bokadev.kompass.presentation.screens.home

import llc.bokadev.kompass.core.presentation.base.BaseEvent

sealed interface HomeEvent : BaseEvent {
    data object LoadHomeData : HomeEvent
}
