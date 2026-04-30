package llc.bokadev.kompass.presentation.screens.nearby

import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.domain.model.PlaceCategory

sealed interface NearbyPlacesEvent : BaseEvent {
    data object LoadNearbyPlaces : NearbyPlacesEvent
    data class SelectCategory(val category: PlaceCategory?) : NearbyPlacesEvent
}
