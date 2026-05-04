package llc.bokadev.kompass.presentation.screens.experiencedetail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.usecase.GetActivityByIdUseCase
import llc.bokadev.kompass.domain.usecase.HasPremiumAccessUseCase

data class ExperienceDetailState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val activity: Experience? = null,
    val hasAudioAccess: Boolean = false,
    val hasDetailAccess: Boolean = false
) : BaseState()

sealed interface ExperienceDetailEvent : BaseEvent {
    data object Retry : ExperienceDetailEvent
}

class ExperienceDetailViewModel(
    private val id: String,
    private val getActivityById: GetActivityByIdUseCase,
    private val hasPremiumAccess: HasPremiumAccessUseCase
) : BaseViewModel<ExperienceDetailState, ExperienceDetailEvent>() {

    override val initialState = ExperienceDetailState()

    init {
        load()
    }

    override fun onIntent(event: ExperienceDetailEvent) {
        when (event) {
            ExperienceDetailEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getActivityById(id)
                .onSuccess { activity ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            activity = activity,
                            hasAudioAccess = hasPremiumAccess(activity.audioAccessTier),
                            hasDetailAccess = hasPremiumAccess(activity.detailAccessTier)
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}
