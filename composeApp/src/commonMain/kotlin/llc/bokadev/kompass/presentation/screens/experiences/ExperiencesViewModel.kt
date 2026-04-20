package llc.bokadev.kompass.presentation.screens.experiences

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ExperiencesState(
    val isLoading: Boolean = false
)

sealed interface ExperiencesIntent

sealed interface ExperiencesSideEffect

class ExperiencesViewModel : ViewModel() {

    private val _state = MutableStateFlow(ExperiencesState())
    val state: StateFlow<ExperiencesState> = _state.asStateFlow()

    fun onIntent(intent: ExperiencesIntent) {
        // Handle intents
    }
}
