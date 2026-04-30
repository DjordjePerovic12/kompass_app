package llc.bokadev.kompass.presentation.screens.nearby

import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.PlaceCategory

data class NearbyPlacesState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    val selectedCategory: PlaceCategory? = null,
    val isUsingCurrentLocation: Boolean = false
) : BaseState()
