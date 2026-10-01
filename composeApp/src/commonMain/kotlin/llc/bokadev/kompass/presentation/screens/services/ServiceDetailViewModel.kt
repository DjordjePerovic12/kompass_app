package llc.bokadev.kompass.presentation.screens.services

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.domain.usecase.GetServiceByIdUseCase

data class ServiceDetailState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val service: Service? = null
) : BaseState()

sealed interface ServiceDetailEvent : BaseEvent {
    data object Retry : ServiceDetailEvent
}

class ServiceDetailViewModel(
    private val id: String,
    private val getServiceById: GetServiceByIdUseCase
) : BaseViewModel<ServiceDetailState, ServiceDetailEvent>() {

    override val initialState = ServiceDetailState()

    init { load() }

    override fun onIntent(event: ServiceDetailEvent) {
        when (event) {
            ServiceDetailEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getServiceById(id)
                .onSuccess { service ->
                    _state.update { it.copy(isLoading = false, service = service) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }
}
