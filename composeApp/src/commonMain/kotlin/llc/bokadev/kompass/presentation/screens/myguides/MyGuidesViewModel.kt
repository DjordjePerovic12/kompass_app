package llc.bokadev.kompass.presentation.screens.myguides

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.core.util.AudioGuidePlaybackState
import llc.bokadev.kompass.core.util.AudioGuidePlayer
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PremiumEntitlements
import llc.bokadev.kompass.domain.repository.PlaceRepository
import llc.bokadev.kompass.domain.repository.PremiumRepository
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase
import llc.bokadev.kompass.domain.usecase.GetSignedAudioUrlUseCase

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
    val selectedFilter: GuideFilter = GuideFilter.ALL,
    val activePlaybackKey: String? = null,
    val playback: AudioGuidePlaybackState = AudioGuidePlaybackState()
) : BaseState()

sealed interface MyGuidesEvent : BaseEvent {
    data object Retry : MyGuidesEvent
    data object StopPlayback : MyGuidesEvent
    data class SelectFilter(val filter: GuideFilter) : MyGuidesEvent
    data class PlayPlace(val placeId: String, val deep: Boolean) : MyGuidesEvent
    data class PlayActivity(val activityId: String, val deep: Boolean) : MyGuidesEvent
}

class MyGuidesViewModel(
    private val getActivities: GetActivitiesUseCase,
    private val placeRepository: PlaceRepository,
    private val premiumRepository: PremiumRepository,
    private val getSignedAudioUrl: GetSignedAudioUrlUseCase,
    private val appPreferences: AppPreferences,
    private val audioGuidePlayer: AudioGuidePlayer
) : BaseViewModel<MyGuidesState, MyGuidesEvent>() {

    override val initialState = MyGuidesState()

    init {
        observePlayback()
        load()
    }

    override fun onIntent(event: MyGuidesEvent) {
        when (event) {
            MyGuidesEvent.Retry -> load()
            MyGuidesEvent.StopPlayback -> audioGuidePlayer.stop()
            is MyGuidesEvent.SelectFilter -> _state.update { it.copy(selectedFilter = event.filter) }
            is MyGuidesEvent.PlayPlace -> playPlace(event.placeId, event.deep)
            is MyGuidesEvent.PlayActivity -> playActivity(event.activityId, event.deep)
        }
    }

    private fun observePlayback() {
        viewModelScope.launch {
            audioGuidePlayer.playbackState.collectLatest { playback ->
                _state.update { it.copy(playback = playback) }
            }
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

    private fun playPlace(placeId: String, deep: Boolean) {
        viewModelScope.launch {
            val place = (_state.value.placeGuides + _state.value.deepPlaceGuides).firstOrNull { it.id == placeId } ?: return@launch
            val audioPath = if (deep) place.localizedDeepAudioFile(appPreferences.getSelectedLanguage()) else place.localizedAudioFile(appPreferences.getSelectedLanguage())
            val key = "place-$placeId-$deep"
            if (_state.value.activePlaybackKey == key && _state.value.playback.sourceUrl != null) {
                audioGuidePlayer.togglePlayPause()
                return@launch
            }
            val audioUrl = audioPath?.let { getSignedAudioUrl(it).getOrNull() } ?: return@launch
            _state.update { it.copy(activePlaybackKey = key) }
            audioGuidePlayer.prepare(
                url = audioUrl,
                title = place.localizedName(appPreferences.getSelectedLanguage()),
                subtitle = place.zone.orEmpty().replace('_', ' ').replace('-', ' ')
            )
        }
    }

    private fun playActivity(activityId: String, deep: Boolean) {
        viewModelScope.launch {
            val activity = (_state.value.activityGuides + _state.value.deepActivityGuides).firstOrNull { it.id == activityId } ?: return@launch
            val audioPath = if (deep) activity.localizedDeepAudioFile(appPreferences.getSelectedLanguage()) else activity.localizedAudioFile(appPreferences.getSelectedLanguage())
            val key = "activity-$activityId-$deep"
            if (_state.value.activePlaybackKey == key && _state.value.playback.sourceUrl != null) {
                audioGuidePlayer.togglePlayPause()
                return@launch
            }
            val audioUrl = audioPath?.let { getSignedAudioUrl(it).getOrNull() } ?: return@launch
            _state.update { it.copy(activePlaybackKey = key) }
            audioGuidePlayer.prepare(
                url = audioUrl,
                title = activity.localizedName(appPreferences.getSelectedLanguage()),
                subtitle = activity.localizedLocation(appPreferences.getSelectedLanguage())
            )
        }
    }
}
