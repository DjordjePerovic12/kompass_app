package llc.bokadev.kompass.presentation.screens.mustsee

import llc.bokadev.kompass.core.presentation.base.BaseEvent

sealed interface MustSeePlacesEvent : BaseEvent {
    data object LoadPlaces : MustSeePlacesEvent
}
