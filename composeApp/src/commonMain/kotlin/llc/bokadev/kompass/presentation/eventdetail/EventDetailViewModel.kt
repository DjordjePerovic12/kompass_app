package llc.bokadev.kompass.presentation.eventdetail

import androidx.lifecycle.viewModelScope
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.events_couldnt_load
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess
import llc.bokadev.kompass.domain.usecase.GetEventByIdUseCase
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewModel
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

class EventDetailViewModel(
    private val id: String,
    private val getEventById: GetEventByIdUseCase,
    private val navigator: Navigator,
    private val preferences: AppPreferences
) : BaseViewModel<EventDetailState, EventDetailEvent>() {

    override val initialViewState = EventDetailState()
    private var hasLoadedInitialData = false

    override fun dismissError() {
        state.update { it.copy(error = null) }
    }

    val eventDetailState = state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                loadEvent()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = state.value
        )

    fun onEvent(event: EventDetailEvent) {
        when (event) {
            EventDetailEvent.NavigateBack -> viewModelScope.launch {
                navigator.navigateUp()
            }

            EventDetailEvent.OnRetryClick -> loadEvent()
            EventDetailEvent.OnTicketsClick -> state.value.event?.ticketUrl?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
                openExternal(ExternalLaunch.Url(url))
            }

            EventDetailEvent.OnMapsClick -> state.value.event?.let {
                val lang = preferences.getSelectedLanguage()
                val label = it.localizedVenue(lang).ifBlank { it.localizedName(lang) }
                openExternal(ExternalLaunch.Maps(it.latitude, it.longitude, label))
            }

            is EventDetailEvent.OpenExternal -> Unit
        }
    }

    private fun openExternal(launch: ExternalLaunch) {
        viewModelScope.launch {
            event.emit(EventDetailEvent.OpenExternal(launch))
        }
    }

    private fun loadEvent() {
        viewModelScope.launch {
            state.update { it.copy(isLoading = it.event == null, error = null) }
            getEventById(id)
                .onSuccess { event ->
                    state.update { it.copy(isLoading = false, event = event) }
                }
                .onError { _, _ ->
                    state.update {
                        it.copy(
                            isLoading = false,
                            error = if (it.event == null) BaseError.InlineError(Res.string.events_couldnt_load) else null
                        )
                    }
                }
        }
    }
}
