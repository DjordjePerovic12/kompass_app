package llc.bokadev.kompass.presentation.services.detail

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.states_couldnt_load_service
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.usecase.GetServiceByIdUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class ServiceDetailViewModel(
    private val id: String,
    private val getServiceById: GetServiceByIdUseCase,
    private val navigator: Navigator,
    private val preferences: AppPreferences
) : BaseViewModel<ServiceDetailState, ServiceDetailEvent>() {

    override val initialViewState = ServiceDetailState()
    private var hasLoadedInitialData = false

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val serviceDetailState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                loadService()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: ServiceDetailEvent) {
        val service = state.value.service
        when (event) {
            ServiceDetailEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            ServiceDetailEvent.OnRetryClick -> loadService()
            ServiceDetailEvent.OnWebsiteClick -> service?.externalWebsite?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
                openExternal(ExternalLaunch.Url(url))
            }

            ServiceDetailEvent.OnMapsClick -> {
                val lat = service?.latitude ?: return
                val lng = service.longitude ?: return
                openExternal(ExternalLaunch.Maps(lat, lng, service.localizedName(preferences.getSelectedLanguage())))
            }

            is ServiceDetailEvent.OpenExternal -> Unit
        }
    }

    private fun openExternal(launch: ExternalLaunch) {
        viewModelScope.launch {
            event.emit(ServiceDetailEvent.OpenExternal(launch))
        }
    }

    private fun loadService() {
        viewModelScope.launch {
            state.update { it.copy(isLoading = it.service == null, error = null) }
            getServiceById(id)
                .onSuccess { service ->
                    state.update { it.copy(isLoading = false, service = service) }
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.service == null) BaseError.InlineError(Res.string.states_couldnt_load_service) else null
                        )
                    }
                }
        }
    }
}
