package llc.bokadev.kompass.presentation.screens.services

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.domain.usecase.GetServicesUseCase

data class ServicesState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val services: List<Service> = emptyList()
) : BaseState()

sealed interface ServicesEvent : BaseEvent {
    data object Retry : ServicesEvent
}

class ServicesViewModel(
    private val getServices: GetServicesUseCase
) : BaseViewModel<ServicesState, ServicesEvent>() {

    override val initialState = ServicesState()

    init { load() }

    override fun onIntent(event: ServicesEvent) {
        when (event) {
            ServicesEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getServices()
                .onSuccess { services ->
                    _state.update { it.copy(isLoading = false, services = services) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }
}
