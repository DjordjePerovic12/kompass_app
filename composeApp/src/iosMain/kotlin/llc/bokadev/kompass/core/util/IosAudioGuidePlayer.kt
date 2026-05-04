package llc.bokadev.kompass.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyDefaultPlaybackRate
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import kotlin.math.roundToLong

@OptIn(ExperimentalForeignApi::class)
class IosAudioGuidePlayer : AudioGuidePlayer {
    private val player = AVPlayer()
    private val state = MutableStateFlow(AudioGuidePlaybackState())
    private var timeObserver: Any? = null
    private var remoteCommandsConfigured = false

    override val playbackState: StateFlow<AudioGuidePlaybackState> = state

    override fun prepare(url: String, title: String, subtitle: String) {
        if (url.isBlank()) return

        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)

        val item = NSURL.URLWithString(url)?.let { AVPlayerItem(it) }
        if (item == null) {
            state.value = state.value.copy(error = "Audio could not be loaded.")
            return
        }

        player.replaceCurrentItemWithPlayerItem(item)
        state.value = AudioGuidePlaybackState(
            sourceUrl = url,
            title = title,
            subtitle = subtitle,
            isBuffering = false,
            isPlaying = true
        )
        configureRemoteCommandsIfNeeded()
        installTimeObserver()
        player.play()
        updateNowPlayingInfo()
    }

    override fun togglePlayPause() {
        if (state.value.isPlaying) {
            player.pause()
            state.value = state.value.copy(isPlaying = false)
        } else {
            player.play()
            state.value = state.value.copy(isPlaying = true)
        }
        updateNowPlayingInfo()
    }

    override fun seekTo(positionMs: Long) {
        val seconds = positionMs.toDouble() / 1000.0
        player.seekToTime(
            CMTimeMakeWithSeconds(seconds, preferredTimescale = 600)
        )
        state.value = state.value.copy(progressMs = positionMs)
        updateNowPlayingInfo()
    }

    override fun stop() {
        player.pause()
        state.value = AudioGuidePlaybackState()
        timeObserver?.let { observer ->
            player.removeTimeObserver(observer)
            timeObserver = null
        }
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
    }

    private fun installTimeObserver() {
        timeObserver?.let { observer ->
            player.removeTimeObserver(observer)
        }

        timeObserver = player.addPeriodicTimeObserverForInterval(
            interval = CMTimeMakeWithSeconds(1.0, preferredTimescale = 600),
            queue = null
        ) { time ->
            val currentMs = (CMTimeGetSeconds(time) * 1000.0).roundToLong()
            val durationSeconds = player.currentItem?.duration?.let { CMTimeGetSeconds(it) } ?: 0.0
            val durationMs = (durationSeconds * 1000.0).roundToLong().coerceAtLeast(0L)

            state.value = state.value.copy(
                progressMs = currentMs,
                durationMs = durationMs,
                isPlaying = player.rate > 0.0f
            )
            updateNowPlayingInfo()
        }
    }

    private fun configureRemoteCommandsIfNeeded() {
        if (remoteCommandsConfigured) return
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        commandCenter.playCommand.addTargetWithHandler {
            if (!state.value.isPlaying) {
                player.play()
                state.value = state.value.copy(isPlaying = true)
                updateNowPlayingInfo()
            }
            MPRemoteCommandHandlerStatusSuccess
        }
        commandCenter.pauseCommand.addTargetWithHandler {
            if (state.value.isPlaying) {
                player.pause()
                state.value = state.value.copy(isPlaying = false)
                updateNowPlayingInfo()
            }
            MPRemoteCommandHandlerStatusSuccess
        }
        remoteCommandsConfigured = true
    }

    private fun updateNowPlayingInfo() {
        val current = state.value
        if (current.sourceUrl == null) return

        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = mapOf(
            MPMediaItemPropertyTitle to current.title,
            MPMediaItemPropertyArtist to current.subtitle,
            MPNowPlayingInfoPropertyElapsedPlaybackTime to (current.progressMs.toDouble() / 1000.0),
            MPNowPlayingInfoPropertyPlaybackRate to (if (current.isPlaying) 1.0 else 0.0),
            MPNowPlayingInfoPropertyDefaultPlaybackRate to 1.0
        )
    }
}
