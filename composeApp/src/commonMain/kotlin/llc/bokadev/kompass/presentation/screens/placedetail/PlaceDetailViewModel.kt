package llc.bokadev.kompass.presentation.screens.placedetail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.usecase.GetPlaceByIdUseCase
import llc.bokadev.kompass.domain.usecase.HasPremiumAccessUseCase

class PlaceDetailViewModel(
    private val getPlaceById: GetPlaceByIdUseCase,
    private val hasPremiumAccess: HasPremiumAccessUseCase
) : BaseViewModel<PlaceDetailState, PlaceDetailEvent>() {

    override val initialState = PlaceDetailState()

    override fun onIntent(event: PlaceDetailEvent) {
        when (event) {
            is PlaceDetailEvent.LoadPlace -> loadPlace(event.id)
        }
    }

    private fun loadPlace(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getPlaceById(id)
                .onSuccess { place ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            place = place,
                            hasAudioAccess = hasPremiumAccess(place.audioAccessTier),
                            hasDetailAccess = hasPremiumAccess(place.detailAccessTier)
                        )
                    }
                }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }
}
