package llc.bokadev.kompass.presentation.infocenter.detail

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.states_couldnt_load_update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.usecase.GetInfoNoticeByIdUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class InfoCenterDetailViewModel(
    private val id: String,
    private val getInfoNoticeById: GetInfoNoticeByIdUseCase,
    private val navigator: Navigator
) : BaseViewModel<InfoCenterDetailState, InfoCenterDetailEvent>() {

    override val initialViewState = InfoCenterDetailState()
    private var hasLoadedInitialData = false

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val infoCenterDetailState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                loadNotice()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: InfoCenterDetailEvent) {
        when (event) {
            InfoCenterDetailEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            InfoCenterDetailEvent.OnRetryClick -> loadNotice()
            InfoCenterDetailEvent.OnSourceClick -> state.value.notice?.externalUrl?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
                openExternal(ExternalLaunch.Url(url))
            }

            is InfoCenterDetailEvent.OpenExternal -> Unit
        }
    }

    private fun openExternal(launch: ExternalLaunch) {
        viewModelScope.launch {
            event.emit(InfoCenterDetailEvent.OpenExternal(launch))
        }
    }

    private fun loadNotice() {
        viewModelScope.launch {
            state.update { it.copy(isLoading = it.notice == null, error = null) }
            getInfoNoticeById(id)
                .onSuccess { notice ->
                    state.update { it.copy(isLoading = false, notice = notice) }
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.notice == null) BaseError.InlineError(Res.string.states_couldnt_load_update) else null
                        )
                    }
                }
        }
    }
}
