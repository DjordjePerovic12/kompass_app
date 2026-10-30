package llc.bokadev.kompass.core.audio

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.utils.AudioGuidePlaybackState
import llc.bokadev.kompass.core.utils.AudioGuidePlayer
import llc.bokadev.kompass.core.utils.PlaybackRequestGuard
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.onError
import llc.bokadev.kompass.core.utils.networking.onSuccess

/**
 * The single app-wide owner of center-button playback (place detail spec §2.5). Wraps the
 * existing platform [AudioGuidePlayer] (Android foreground service + media session, iOS AVPlayer
 * + now-playing info), adding ownership and the stale-request rule.
 *
 * Race rule: every intent that starts or stops playback takes a token from the shared
 * [PlaybackRequestGuard]. URL resolution (signed URL / offline cache) is the slow step, and its
 * result is applied only if that token is still current **and** the same screen still owns
 * playback. `MainShell` invalidates the same guard on every destination change and tab switch,
 * so "the track starts after I left" can't happen from any exit path.
 */
class AudioController(
    private val player: AudioGuidePlayer,
    private val guard: PlaybackRequestGuard,
    private val resolveUrl: suspend (audioPath: String) -> Resource<String, NetworkError, String?>,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow<AudioState>(AudioState.Idle)
    val state: StateFlow<AudioState> = _state.asStateFlow()

    private var ownerKey: String? = null
    private var loadJob: Job? = null

    /** The track handed to the player, and its resolved URL, once prepare() was called. */
    private var active: Active? = null

    private data class Active(val track: AudioTrack, val url: String, val requestId: Long, var seenByPlayer: Boolean = false)

    init {
        scope.launch { player.playbackState.collect(::onPlayerState) }
    }

    /** Starts [track] for [owner], or resumes it if it is the same, paused track. */
    fun play(track: AudioTrack, owner: String) {
        val current = _state.value
        if (current is AudioState.Paused && current.track.id == track.id && owner == ownerKey) {
            player.togglePlayPause()
            return
        }
        val requestId = guard.newToken()
        ownerKey = owner
        loadJob?.cancel()
        if (active != null) {
            active = null
            player.stop()
        }
        _state.value = AudioState.Loading(track, requestId)
        loadJob = scope.launch {
            val result: Resource<String, NetworkError, String?> = try {
                resolveUrl(track.audioPath)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Resource.Error(NetworkError.UNKNOWN, error.message)
            }
            // Stale guard: a newer intent, a navigation, or another owner superseded this load.
            if (!guard.isCurrent(requestId) || ownerKey != owner) {
                val state = _state.value
                if (state is AudioState.Loading && state.requestId == requestId) _state.value = AudioState.Idle
                return@launch
            }
            result
                .onSuccess { url ->
                    active = Active(track, url, requestId)
                    player.prepare(url = url, title = track.title, subtitle = track.subtitle)
                }
                .onError { _, _ -> _state.value = AudioState.Failed(track, AudioState.Reason.Network) }
        }
    }

    /**
     * The library's row/card button: the current track pauses, resumes or cancels its load;
     * any other track replaces it (one active track app-wide).
     */
    fun toggle(track: AudioTrack, owner: String) {
        when (val current = _state.value) {
            is AudioState.Loading -> if (current.track.id == track.id) return cancelLoading(owner)
            is AudioState.Playing -> if (current.track.id == track.id) return pause()
            else -> Unit
        }
        play(track, owner)
    }

    /** Seeks the loaded track; ignored while nothing is playing or paused. */
    fun seekTo(positionMs: Long) {
        val (_, duration) = positionAndDuration() ?: return
        player.seekTo(positionMs.coerceIn(0L, duration ?: Long.MAX_VALUE))
    }

    /** ±15s from the card. Clamped to the track. */
    fun skipBy(deltaMs: Long) {
        val (position, duration) = positionAndDuration() ?: return
        player.seekTo((position + deltaMs).coerceIn(0L, duration ?: Long.MAX_VALUE))
    }

    private fun positionAndDuration(): Pair<Long, Long?>? = when (val current = _state.value) {
        is AudioState.Playing -> current.positionMs to current.durationMs
        is AudioState.Paused -> current.positionMs to current.durationMs
        else -> null
    }

    fun pause() {
        if (_state.value is AudioState.Playing) player.togglePlayPause()
    }

    /** Cancels a pending load (the Loading-state tap), keeping ownership. */
    fun cancelLoading(owner: String) {
        if (owner != ownerKey || _state.value !is AudioState.Loading) return
        guard.invalidate()
        loadJob?.cancel()
        if (active != null) {
            active = null
            player.stop()
        }
        _state.value = AudioState.Idle
    }

    /** Called when the owning screen leaves. A no-op if another screen owns playback by now. */
    fun stopIfOwner(owner: String) {
        if (owner != ownerKey) return
        guard.invalidate()
        loadJob?.cancel()
        if (active != null) player.stop()
        active = null
        ownerKey = null
        _state.value = AudioState.Idle
    }

    private fun onPlayerState(playback: AudioGuidePlaybackState) {
        val current = active ?: return
        if (playback.sourceUrl != current.url) {
            // The player stopped (MainShell navigation stop, notification Stop) or moved on to
            // another source after we had seen ours: our track is no longer playing.
            if (current.seenByPlayer) {
                active = null
                _state.value = AudioState.Idle
            }
            return
        }
        current.seenByPlayer = true
        val duration = playback.durationMs.takeIf { it > 0 }
        _state.value = when {
            playback.error != null -> {
                active = null
                player.stop()
                AudioState.Failed(current.track, AudioState.Reason.Decode)
            }
            playback.isCompleted -> {
                // Track ended: back to Ready with the position reset (spec §2.3 rule 8).
                active = null
                player.stop()
                AudioState.Idle
            }
            playback.isBuffering -> AudioState.Loading(current.track, current.requestId)
            playback.isPlaying -> AudioState.Playing(current.track, playback.progressMs, duration)
            else -> AudioState.Paused(current.track, playback.progressMs, duration)
        }
    }
}
