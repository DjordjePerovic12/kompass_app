package llc.bokadev.kompass.presentation.screens.myguides

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.PremiumEntitlements
import llc.bokadev.kompass.domain.repository.PremiumRepository
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase

data class MyGuidesState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val entitlements: PremiumEntitlements = PremiumEntitlements(),
    val audioGuides: List<Experience> = emptyList(),
    val premiumActivities: List<Experience> = emptyList()
) : BaseState()

sealed interface MyGuidesEvent : BaseEvent {
    data object Retry : MyGuidesEvent
}

class MyGuidesViewModel(
    private val getActivities: GetActivitiesUseCase,
    private val premiumRepository: PremiumRepository
) : BaseViewModel<MyGuidesState, MyGuidesEvent>() {

    override val initialState = MyGuidesState()

    init {
        load()
    }

    override fun onIntent(event: MyGuidesEvent) {
        when (event) {
            MyGuidesEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            val entitlements = premiumRepository.getEntitlements()
            _state.update { it.copy(isLoading = true, error = null, entitlements = entitlements) }
            getActivities()
                .onSuccess { activities ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            audioGuides = activities.filter { activity ->
                                activity.audioFile != null && entitlements.hasAccess(activity.audioAccessTier)
                            },
                            premiumActivities = activities.filter { activity ->
                                entitlements.hasAccess(activity.detailAccessTier) || entitlements.hasAccess(activity.audioAccessTier)
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}
