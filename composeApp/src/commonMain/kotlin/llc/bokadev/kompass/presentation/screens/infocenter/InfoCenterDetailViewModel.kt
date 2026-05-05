package llc.bokadev.kompass.presentation.screens.infocenter

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.usecase.GetInfoNoticeByIdUseCase

data class InfoCenterDetailState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val notice: InfoNotice? = null
) : BaseState()

sealed interface InfoCenterDetailEvent : BaseEvent {
    data object Retry : InfoCenterDetailEvent
}

class InfoCenterDetailViewModel(
    private val id: String,
    private val getInfoNoticeById: GetInfoNoticeByIdUseCase
) : BaseViewModel<InfoCenterDetailState, InfoCenterDetailEvent>() {

    override val initialState = InfoCenterDetailState()

    init {
        load()
    }

    override fun onIntent(event: InfoCenterDetailEvent) {
        when (event) {
            InfoCenterDetailEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getInfoNoticeById(id)
                .onSuccess { notice -> _state.update { it.copy(isLoading = false, notice = notice) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }
}
