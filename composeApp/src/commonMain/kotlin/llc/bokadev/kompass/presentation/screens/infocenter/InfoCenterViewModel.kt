package llc.bokadev.kompass.presentation.screens.infocenter

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase

data class InfoCenterState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val notices: List<InfoNotice> = emptyList()
) : BaseState()

sealed interface InfoCenterEvent : BaseEvent {
    data object Retry : InfoCenterEvent
}

class InfoCenterViewModel(
    private val getCurrentInfoNotices: GetCurrentInfoNoticesUseCase
) : BaseViewModel<InfoCenterState, InfoCenterEvent>() {

    override val initialState = InfoCenterState()

    init {
        load()
    }

    override fun onIntent(event: InfoCenterEvent) {
        when (event) {
            InfoCenterEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getCurrentInfoNotices()
                .onSuccess { notices -> _state.update { it.copy(isLoading = false, notices = notices) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }
}
