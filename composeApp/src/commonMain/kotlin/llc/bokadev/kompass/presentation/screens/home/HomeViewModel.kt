package llc.bokadev.kompass.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.usecase.GetMustSeePlacesUseCase

data class HomeState(
    val isLoading: Boolean = false,
    val mustSeePlaces: List<Place> = emptyList(),
    val error: String? = null
)

sealed interface HomeIntent {
    data object LoadMustSeePlaces : HomeIntent
}

sealed interface HomeSideEffect

class HomeViewModel(
    private val getMustSeePlaces: GetMustSeePlacesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        onIntent(HomeIntent.LoadMustSeePlaces)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadMustSeePlaces -> loadMustSeePlaces()
        }
    }

    private fun loadMustSeePlaces() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getMustSeePlaces()
                .onSuccess { places ->
                    _state.update { it.copy(isLoading = false, mustSeePlaces = places) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }
}
