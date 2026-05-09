package llc.bokadev.kompass.presentation.screens.mustsee

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.usecase.GetMustSeePlacesUseCase

class MustSeePlacesViewModel(
    private val getMustSeePlaces: GetMustSeePlacesUseCase
) : BaseViewModel<MustSeePlacesState, MustSeePlacesEvent>() {

    override val initialState = MustSeePlacesState()

    fun init() {
        if (_state.value.places.isEmpty() && !_state.value.isLoading) {
            loadPlaces()
        }
    }

    override fun onIntent(event: MustSeePlacesEvent) {
        when (event) {
            MustSeePlacesEvent.LoadPlaces -> loadPlaces()
        }
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getMustSeePlaces()
                .onSuccess { places -> _state.update { it.copy(isLoading = false, places = places) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }
}
