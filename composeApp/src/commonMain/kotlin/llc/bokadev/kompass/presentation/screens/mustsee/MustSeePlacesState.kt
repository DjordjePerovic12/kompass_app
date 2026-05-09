package llc.bokadev.kompass.presentation.screens.mustsee

import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.domain.model.Place

data class MustSeePlacesState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val places: List<Place> = emptyList()
) : BaseState()
