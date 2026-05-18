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

enum class GuideFilter {
    ALL,
    DEEP
}

data class MyGuidesState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val entitlements: PremiumEntitlements = PremiumEntitlements(),
    val placeGuides: List<Place> = emptyList(),
    val activityGuides: List<Experience> = emptyList(),
    val deepPlaceGuides: List<Place> = emptyList(),
    val deepActivityGuides: List<Experience> = emptyList(),
    val selectedFilter: GuideFilter = GuideFilter.ALL
) : BaseState()

sealed interface MyGuidesEvent : BaseEvent {
    data object Retry : MyGuidesEvent
    data class SelectFilter(val filter: GuideFilter) : MyGuidesEvent
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
            is MyGuidesEvent.SelectFilter -> _state.update { it.copy(selectedFilter = event.filter) }
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

            val places = placesResult.getOrDefault(emptyList())
            val activities = activitiesResult.getOrDefault(emptyList())

            _state.update {
                it.copy(
                    isLoading = false,
                    entitlements = entitlements,
                    placeGuides = places.filter { place -> !place.audioFile.isNullOrEmpty() }.sortedBy { place -> place.sortOrder },
                    activityGuides = activities.filter { activity -> !activity.audioFile.isNullOrEmpty() }.sortedBy { activity -> activity.sortOrder },
                    deepPlaceGuides = places.filter { place -> entitlements.audioPass && !place.deepAudioFile.isNullOrEmpty() }.sortedBy { place -> place.sortOrder },
                    deepActivityGuides = activities.filter { activity -> entitlements.audioPass && !activity.deepAudioFile.isNullOrEmpty() }.sortedBy { activity -> activity.sortOrder },
                    selectedFilter = if (!entitlements.audioPass) GuideFilter.ALL else it.selectedFilter
                )
            }
        }
    }
}
