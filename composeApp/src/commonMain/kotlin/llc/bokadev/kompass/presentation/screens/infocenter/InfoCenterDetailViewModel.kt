package llc.bokadev.kompass.presentation.screens.infocenter

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.usecase.GetInfoNoticeByIdUseCase
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class InfoCenterDetailViewModel(
    private val id: String,
    private val getInfoNoticeById: GetInfoNoticeByIdUseCase,
    private val navigator: Navigator
) : BaseViewModel<InfoCenterDetailState, InfoCenterDetailAction, InfoCenterDetailEvent>(InfoCenterDetailState()) {

    override suspend fun onFirstStart() = load()

    override fun onAction(action: InfoCenterDetailAction) {
        when (action) {
            InfoCenterDetailAction.Retry -> viewModelScope.launch { load() }
            InfoCenterDetailAction.Back -> navigator.navigateUp()
            InfoCenterDetailAction.OpenSource -> state.value.notice?.externalUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
                sendEvent(InfoCenterDetailEvent.Launch(ExternalLaunch.Url(it)))
            }
        }
    }

    private suspend fun load() {
        updateState { it.copy(isLoading = it.notice == null, error = null) }
        getInfoNoticeById(id)
            .onSuccess { notice -> updateState { it.copy(isLoading = false, notice = notice) } }
            .onError { _, _ -> updateState { it.copy(isLoading = false, error = if (it.notice == null) LOAD_FAILED else null) } }
    }

    private companion object {
        val LOAD_FAILED = UiText { it.states.couldntLoadUpdate }
    }
}
