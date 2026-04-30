package llc.bokadev.kompass.presentation.screens.placedetail

import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.domain.model.Place

data class PlaceDetailState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val place: Place? = null
) : BaseState()
