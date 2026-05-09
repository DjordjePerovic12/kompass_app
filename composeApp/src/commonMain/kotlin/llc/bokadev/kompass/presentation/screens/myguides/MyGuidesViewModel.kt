package llc.bokadev.kompass.presentation.screens.myguides

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PremiumEntitlements
import llc.bokadev.kompass.domain.repository.PlaceRepository
import llc.bokadev.kompass.domain.repository.PremiumRepository
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase

data class MyGuidesState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val entitlements: PremiumEntitlements = PremiumEntitlements(),
    val placeGuides: List<Place> = emptyList(),
    val activityGuides: List<Experience> = emptyList()
) : BaseState()

sealed interface MyGuidesEvent : BaseEvent {
    data object Retry : MyGuidesEvent
}

class MyGuidesViewModel(
    private val getActivities: GetActivitiesUseCase,
    private val placeRepository: PlaceRepository,
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

            val placesDeferred = async { placeRepository.getActivePlaces() }
            val activitiesDeferred = async { getActivities() }

            val placesResult = placesDeferred.await()
            val activitiesResult = activitiesDeferred.await()

            val firstFailure = placesResult.exceptionOrNull() ?: activitiesResult.exceptionOrNull()
            if (firstFailure != null &&
                placesResult.getOrNull().isNullOrEmpty() &&
                activitiesResult.getOrNull().isNullOrEmpty()
            ) {
                _state.update { it.copy(isLoading = false, error = firstFailure.message) }
                return@launch
            }

            val placeGuides = placesResult.getOrDefault(emptyList())
                .filter { place -> !place.audioFile.isNullOrBlank() && entitlements.hasAccess(place.audioAccessTier) }
                .sortedBy { it.sortOrder }

            val activityGuides = activitiesResult.getOrDefault(emptyList())
                .filter { activity -> !activity.audioFile.isNullOrBlank() && entitlements.hasAccess(activity.audioAccessTier) }
                .sortedBy { it.sortOrder }

            _state.update {
                it.copy(
                    isLoading = false,
                    placeGuides = placeGuides,
                    activityGuides = activityGuides
                )
            }
        }
    }
}
