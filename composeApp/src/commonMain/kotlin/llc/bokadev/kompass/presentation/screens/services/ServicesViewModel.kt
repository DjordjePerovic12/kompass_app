package llc.bokadev.kompass.presentation.screens.services

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.domain.usecase.GetServicesUseCase
import llc.bokadev.kompass.core.navigation.Screen

class ServicesViewModel(
    private val getServices: GetServicesUseCase,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<ServicesState, ServicesAction, Nothing>(ServicesState()) {

    private var loadJob: Job? = null

    override suspend fun onFirstStart() {
        analytics.trackScreenView("services")
        load()
    }

    override fun onAction(action: ServicesAction) {
        when (action) {
            ServicesAction.Retry -> load()
            ServicesAction.Back -> navigator.navigateUp()
            is ServicesAction.OpenService -> navigator.navigateTo(Screen.ServiceDetailScreen(action.id))
        }
    }

    /** Keeps what's on screen while retrying; a newer load cancels an older one. */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = it.services.isEmpty(), error = null, refreshFailed = false) }
            getServices()
                .onSuccess { items -> updateState { it.copy(isLoading = false, services = items) } }
                .onFailure {
                    updateState {
                        it.copy(isLoading = false, error = if (it.services.isEmpty()) LOAD_FAILED else null, refreshFailed = it.services.isNotEmpty())
                    }
                }
        }
    }

    private companion object {
        val LOAD_FAILED = UiText { it.states.couldntLoadServices }
    }
}
