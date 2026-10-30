package llc.bokadev.kompass.presentation.screens.services

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.domain.usecase.GetServiceByIdUseCase
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class ServiceDetailViewModel(
    private val id: String,
    private val getServiceById: GetServiceByIdUseCase,
    private val navigator: Navigator,
    private val preferences: AppPreferences
) : BaseViewModel<ServiceDetailState, ServiceDetailAction, ServiceDetailEvent>(ServiceDetailState()) {

    override suspend fun onFirstStart() = load()

    override fun onAction(action: ServiceDetailAction) {
        val service = state.value.service
        when (action) {
            ServiceDetailAction.Retry -> viewModelScope.launch { load() }
            ServiceDetailAction.Back -> navigator.navigateUp()
            ServiceDetailAction.OpenWebsite -> service?.externalWebsite?.trim()?.takeIf { it.isNotBlank() }?.let {
                sendEvent(ServiceDetailEvent.Launch(ExternalLaunch.Url(it)))
            }
            ServiceDetailAction.OpenMaps -> {
                val lat = service?.latitude ?: return
                val lng = service.longitude ?: return
                sendEvent(ServiceDetailEvent.Launch(ExternalLaunch.Maps(lat, lng, service.localizedName(preferences.getSelectedLanguage()))))
            }
        }
    }

    private suspend fun load() {
        updateState { it.copy(isLoading = it.service == null, error = null) }
        getServiceById(id)
            .onSuccess { service -> updateState { it.copy(isLoading = false, service = service) } }
            .onFailure { updateState { it.copy(isLoading = false, error = if (it.service == null) LOAD_FAILED else null) } }
    }

    private companion object {
        val LOAD_FAILED = UiText { it.states.couldntLoadService }
    }
}
