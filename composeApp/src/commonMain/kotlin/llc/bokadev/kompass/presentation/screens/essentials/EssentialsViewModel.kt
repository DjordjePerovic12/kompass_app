package llc.bokadev.kompass.presentation.screens.essentials

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EssentialsState(
    val isLoading: Boolean = false
)

sealed interface EssentialsIntent

sealed interface EssentialsSideEffect

class EssentialsViewModel : ViewModel() {

    private val _state = MutableStateFlow(EssentialsState())
    val state: StateFlow<EssentialsState> = _state.asStateFlow()

    fun onIntent(intent: EssentialsIntent) {
        // Handle intents
    }
}
