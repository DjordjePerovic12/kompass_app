package llc.bokadev.kompass.presentation.screens.eventdetail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.usecase.GetEventByIdUseCase

data class EventDetailState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val event: Event? = null
) : BaseState()

sealed interface EventDetailEvent : BaseEvent {
    data object Retry : EventDetailEvent
}

class EventDetailViewModel(
    private val id: String,
    private val getEventById: GetEventByIdUseCase
) : BaseViewModel<EventDetailState, EventDetailEvent>() {

    override val initialState = EventDetailState()

    init {
        load()
    }

    override fun onIntent(event: EventDetailEvent) {
        when (event) {
            EventDetailEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getEventById(id)
                .onSuccess { event ->
                    _state.update { it.copy(isLoading = false, event = event) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}
