package llc.bokadev.kompass.presentation.screens.nearby

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.usecase.GetNearbyPlacesUseCase

class NearbyPlacesViewModel(
    private val getNearbyPlaces: GetNearbyPlacesUseCase,
    private val userLocationProvider: UserLocationProvider
) : BaseViewModel<NearbyPlacesState, NearbyPlacesEvent>() {

    override val initialState = NearbyPlacesState()

    init {
        loadNearbyPlaces()
    }

    override fun onIntent(event: NearbyPlacesEvent) {
        when (event) {
            NearbyPlacesEvent.LoadNearbyPlaces -> loadNearbyPlaces()
            is NearbyPlacesEvent.SelectCategory -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }
        }
    }

    private fun loadNearbyPlaces() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentLocation = userLocationProvider.getCurrentLocation()
            val origin = currentLocation ?: KOTOR_OLD_TOWN_CENTER

            getNearbyPlaces(origin)
                .onSuccess { nearbyPlaces ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            nearbyPlaces = nearbyPlaces,
                            isUsingCurrentLocation = currentLocation != null
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }

    private companion object {
        val KOTOR_OLD_TOWN_CENTER = GeoPoint(
            latitude = 42.4246,
            longitude = 18.7712
        )
    }
}
