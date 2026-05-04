package llc.bokadev.kompass.presentation.screens.experiencedetail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.core.util.AudioGuidePlaybackState
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.usecase.GetActivityByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetSignedAudioUrlUseCase
import llc.bokadev.kompass.domain.usecase.HasPremiumAccessUseCase
import llc.bokadev.kompass.core.util.AudioGuidePlayer

data class ExperienceGuideState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val activity: Experience? = null,
    val hasAudioAccess: Boolean = false,
    val hasDetailAccess: Boolean = false,
    val audioUrl: String? = null,
    val currentLocation: GeoPoint? = null,
    val playback: AudioGuidePlaybackState = AudioGuidePlaybackState()
) : BaseState()

sealed interface ExperienceGuideEvent : BaseEvent {
    data object Retry : ExperienceGuideEvent
    data object TogglePlayPause : ExperienceGuideEvent
    data class SeekTo(val positionMs: Long) : ExperienceGuideEvent
}

class ExperienceGuideViewModel(
    private val id: String,
    private val getActivityById: GetActivityByIdUseCase,
    private val getSignedAudioUrl: GetSignedAudioUrlUseCase,
    private val hasPremiumAccess: HasPremiumAccessUseCase,
    private val userLocationProvider: UserLocationProvider,
    private val audioGuidePlayer: AudioGuidePlayer
) : BaseViewModel<ExperienceGuideState, ExperienceGuideEvent>() {

    override val initialState = ExperienceGuideState()

    init {
        observePlayback()
        load()
    }

    override fun onIntent(event: ExperienceGuideEvent) {
        when (event) {
            ExperienceGuideEvent.Retry -> load()
            ExperienceGuideEvent.TogglePlayPause -> audioGuidePlayer.togglePlayPause()
            is ExperienceGuideEvent.SeekTo -> audioGuidePlayer.seekTo(event.positionMs)
        }
    }

    private fun observePlayback() {
        viewModelScope.launch {
            audioGuidePlayer.playbackState.collect { playback ->
                _state.update { it.copy(playback = playback) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getActivityById(id)
                .onSuccess { activity ->
                    val hasAudioAccess = hasPremiumAccess(activity.audioAccessTier)
                    val hasDetailAccess = hasPremiumAccess(activity.detailAccessTier)
                    val currentLocation = runCatching { userLocationProvider.getCurrentLocation() }.getOrNull()
                    val audioUrl = resolveAudioUrl(activity, hasAudioAccess)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            activity = activity,
                            hasAudioAccess = hasAudioAccess,
                            hasDetailAccess = hasDetailAccess,
                            currentLocation = currentLocation,
                            audioUrl = audioUrl
                        )
                    }

                    if (audioUrl != null) {
                        audioGuidePlayer.prepare(
                            url = audioUrl,
                            title = activity.localizedName("en"),
                            subtitle = activity.localizedLocation("en")
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private suspend fun resolveAudioUrl(activity: Experience, hasAudioAccess: Boolean): String? {
        val audioPath = activity.audioFile ?: return null
        if (!hasAudioAccess) return null
        return getSignedAudioUrl(audioPath).getOrNull()
    }
}
