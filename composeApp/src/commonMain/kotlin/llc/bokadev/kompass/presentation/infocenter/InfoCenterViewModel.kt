package llc.bokadev.kompass.presentation.infocenter

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.states_couldnt_load_updates
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel

class InfoCenterViewModel(
    private val getCurrentInfoNotices: GetCurrentInfoNoticesUseCase,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<InfoCenterState, InfoCenterEvent>() {

    override val initialViewState = InfoCenterState()
    private var hasLoadedInitialData = false
    private var loadJob: Job? = null

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val infoCenterState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                analytics.trackScreenView("info_center")
                load()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: InfoCenterEvent) {
        when (event) {
            InfoCenterEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            InfoCenterEvent.OnRetryClick -> load()
            is InfoCenterEvent.OnNoticeClick -> {
                state.value.notices.firstOrNull { it.id == event.id }?.let {
                    analytics.trackInfoNoticeView(noticeId = it.id, cityId = it.cityId, contentOrigin = "info_center")
                }
                viewModelScope.launch {
                    navigator.navigateTo(Screen.InfoCenterDetailScreen(event.id))
                }
            }
        }
    }

    /** Keeps what's on screen while retrying; a newer load cancels an older one. */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoading = it.notices.isEmpty(), error = null, refreshFailed = false) }
            getCurrentInfoNotices()
                .onSuccess { items -> state.update { it.copy(isLoading = false, notices = items) } }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.notices.isEmpty()) BaseError.InlineError(Res.string.states_couldnt_load_updates) else null,
                            refreshFailed = it.notices.isNotEmpty()
                        )
                    }
                }
        }
    }
}
