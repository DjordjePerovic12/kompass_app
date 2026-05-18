package llc.bokadev.kompass.presentation.screens.placedetail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.core.util.AudioGuidePlaybackState
import llc.bokadev.kompass.core.util.AudioGuidePlayer
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.usecase.GetPlaceByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetSignedAudioUrlUseCase
import llc.bokadev.kompass.domain.usecase.HasPremiumAccessUseCase

data class PlaceGuideState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val place: Place? = null,
    val hasAudioAccess: Boolean = false,
    val hasDetailAccess: Boolean = false,
    val audioUrl: String? = null,
    val currentLocation: GeoPoint? = null,
    val playback: AudioGuidePlaybackState = AudioGuidePlaybackState()
) : BaseState()

sealed interface PlaceGuideEvent : BaseEvent {
    data object Retry : PlaceGuideEvent
    data object TogglePlayPause : PlaceGuideEvent
    data object StopPlayback : PlaceGuideEvent
    data class SeekTo(val positionMs: Long) : PlaceGuideEvent
}

class PlaceGuideViewModel(
    private val id: String,
    private val autoplay: Boolean,
    private val deep: Boolean,
    private val getPlaceById: GetPlaceByIdUseCase,
    private val getSignedAudioUrl: GetSignedAudioUrlUseCase,
    private val hasPremiumAccess: HasPremiumAccessUseCase,
    private val userLocationProvider: UserLocationProvider,
    private val appPreferences: AppPreferences,
    private val audioGuidePlayer: AudioGuidePlayer
) : BaseViewModel<PlaceGuideState, PlaceGuideEvent>() {

    override val initialState = PlaceGuideState()
    private var pendingAutoplay = autoplay

    init {
        observePlayback()
        load()
    }

    override fun onIntent(event: PlaceGuideEvent) {
        when (event) {
            PlaceGuideEvent.Retry -> load()
            PlaceGuideEvent.TogglePlayPause -> togglePlayback()
            PlaceGuideEvent.StopPlayback -> audioGuidePlayer.stop()
            is PlaceGuideEvent.SeekTo -> audioGuidePlayer.seekTo(event.positionMs)
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

            getPlaceById(id)
                .onSuccess { place ->
                    val hasDeepAccess = hasPremiumAccess("audio_pass")
                    val currentLocation = runCatching { userLocationProvider.getCurrentLocation() }.getOrNull()
                    val audioUrl = resolveAudioUrl(place, hasDeepAccess)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            place = place,
                            hasAudioAccess = if (deep) hasDeepAccess else !place.audioFile.isNullOrEmpty(),
                            hasDetailAccess = hasDeepAccess,
                            currentLocation = currentLocation,
                            audioUrl = audioUrl
                        )
                    }

                    if (pendingAutoplay && audioUrl != null) {
                        pendingAutoplay = false
                        startPlayback(place, audioUrl)
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun togglePlayback() {
        val current = _state.value
        val audioUrl = current.audioUrl ?: return
        val place = current.place ?: return

        if (current.playback.sourceUrl != audioUrl) {
            startPlayback(place, audioUrl)
            return
        }

        audioGuidePlayer.togglePlayPause()
    }

    private suspend fun resolveAudioUrl(place: Place, hasDeepAccess: Boolean): String? {
        val lang = appPreferences.getSelectedLanguage()
        val audioPath = if (deep) {
            if (!hasDeepAccess) return null
            place.localizedDeepAudioFile(lang)
        } else {
            place.localizedAudioFile(lang)
        } ?: return null
        return getSignedAudioUrl(audioPath).getOrNull()
    }

    private fun startPlayback(place: Place, audioUrl: String) {
        val selectedLanguage = appPreferences.getSelectedLanguage()
        audioGuidePlayer.prepare(
            url = audioUrl,
            title = place.localizedName(selectedLanguage),
            subtitle = place.zone.orEmpty().replace('_', ' ').replace('-', ' ')
        )
    }
}
