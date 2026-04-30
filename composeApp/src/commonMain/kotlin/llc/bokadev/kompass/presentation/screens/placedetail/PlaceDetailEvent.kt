package llc.bokadev.kompass.presentation.screens.placedetail

import llc.bokadev.kompass.core.presentation.base.BaseEvent

sealed interface PlaceDetailEvent : BaseEvent {
    data class LoadPlace(val id: String) : PlaceDetailEvent
}
