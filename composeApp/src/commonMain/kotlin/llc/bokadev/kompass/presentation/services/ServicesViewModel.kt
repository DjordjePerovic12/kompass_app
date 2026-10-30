package llc.bokadev.kompass.presentation.services

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.states_couldnt_load_services
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
import llc.bokadev.kompass.domain.usecase.GetServicesUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel

class ServicesViewModel(
    private val getServices: GetServicesUseCase,
    private val navigator: Navigator,
    private val analytics: AnalyticsRepository
) : BaseViewModel<ServicesState, ServicesEvent>() {

    override val initialViewState = ServicesState()
    private var hasLoadedInitialData = false
    private var loadJob: Job? = null

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val servicesState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                analytics.trackScreenView("services")
                load()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: ServicesEvent) {
        when (event) {
            ServicesEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            ServicesEvent.OnRetryClick -> load()
            is ServicesEvent.OnServiceClick -> viewModelScope.launch {
                navigator.navigateTo(Screen.ServiceDetailScreen(event.id))
            }
        }
    }

    /** Keeps what's on screen while retrying; a newer load cancels an older one. */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.update { it.copy(isLoading = it.services.isEmpty(), error = null, refreshFailed = false) }
            getServices()
                .onSuccess { items -> state.update { it.copy(isLoading = false, services = items) } }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.services.isEmpty()) BaseError.InlineError(Res.string.states_couldnt_load_services) else null,
                            refreshFailed = it.services.isNotEmpty()
                        )
                    }
                }
        }
    }
}
