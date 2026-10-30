package llc.bokadev.kompass.presentation.screens.infocenter

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.core.navigation.Screen

class InfoCenterViewModel(
    private val getCurrentInfoNotices: GetCurrentInfoNoticesUseCase,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<InfoCenterState, InfoCenterAction, Nothing>(InfoCenterState()) {

    private var loadJob: Job? = null

    override suspend fun onFirstStart() {
        analytics.trackScreenView("info_center")
        load()
    }

    override fun onAction(action: InfoCenterAction) {
        when (action) {
            InfoCenterAction.Retry -> load()
            InfoCenterAction.Back -> navigator.navigateUp()
            is InfoCenterAction.OpenNotice -> {
                state.value.notices.firstOrNull { it.id == action.id }?.let {
                    analytics.trackInfoNoticeView(noticeId = it.id, cityId = it.cityId, contentOrigin = "info_center")
                }
                navigator.navigateTo(Screen.InfoCenterDetailScreen(action.id))
            }
        }
    }

    /** Keeps what's on screen while retrying; a newer load cancels an older one. */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = it.notices.isEmpty(), error = null, refreshFailed = false) }
            getCurrentInfoNotices()
                .onSuccess { items -> updateState { it.copy(isLoading = false, notices = items) } }
                .onError { _, _ ->
                    updateState {
                        it.copy(isLoading = false, error = if (it.notices.isEmpty()) LOAD_FAILED else null, refreshFailed = it.notices.isNotEmpty())
                    }
                }
        }
    }

    private companion object {
        val LOAD_FAILED = UiText { it.states.couldntLoadUpdates }
    }
}
