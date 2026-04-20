package llc.bokadev.kompass.presentation.screens.events

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EventsState(
    val isLoading: Boolean = false
)

sealed interface EventsIntent

sealed interface EventsSideEffect

class EventsViewModel : ViewModel() {

    private val _state = MutableStateFlow(EventsState())
    val state: StateFlow<EventsState> = _state.asStateFlow()

    fun onIntent(intent: EventsIntent) {
        // Handle intents
    }
}
