package llc.bokadev.kompass.presentation.screens.eventdetail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.mvi.BaseViewModel
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.usecase.GetEventByIdUseCase
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class EventDetailViewModel(
    private val id: String,
    private val getEventById: GetEventByIdUseCase,
    private val navigator: Navigator,
    private val preferences: AppPreferences
) : BaseViewModel<EventDetailState, EventDetailAction, EventDetailEvent>(EventDetailState()) {

    override suspend fun onFirstStart() = load()

    override fun onAction(action: EventDetailAction) {
        val event = state.value.event
        when (action) {
            EventDetailAction.Retry -> viewModelScope.launch { load() }
            EventDetailAction.Back -> navigator.navigateUp()
            EventDetailAction.OpenTickets -> event?.ticketUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
                sendEvent(EventDetailEvent.Launch(ExternalLaunch.Url(it)))
            }
            EventDetailAction.OpenMaps -> event?.let {
                val lang = preferences.getSelectedLanguage()
                val label = it.localizedVenue(lang).ifBlank { it.localizedName(lang) }
                sendEvent(EventDetailEvent.Launch(ExternalLaunch.Maps(it.latitude, it.longitude, label)))
            }
        }
    }

    private suspend fun load() {
        updateState { it.copy(isLoading = it.event == null, error = null) }
        getEventById(id)
            .onSuccess { event -> updateState { it.copy(isLoading = false, event = event) } }
            .onFailure { updateState { it.copy(isLoading = false, error = if (it.event == null) LOAD_FAILED else null) } }
    }

    private companion object {
        val LOAD_FAILED = UiText { it.events.couldntLoad }
    }
}
