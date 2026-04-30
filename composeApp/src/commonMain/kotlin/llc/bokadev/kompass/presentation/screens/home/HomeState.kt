package llc.bokadev.kompass.presentation.screens.home

import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.Place

data class HomeState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val mustSeePlaces: List<Place> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    val isUsingCurrentLocation: Boolean = false
) : BaseState()
