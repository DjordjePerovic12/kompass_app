package llc.bokadev.kompass.presentation.screens.home

import llc.bokadev.kompass.domain.model.Place

data class HomeState(
    val isLoading: Boolean = false,
    val mustSeePlaces: List<Place> = emptyList(),
    val error: String? = null
)