package llc.bokadev.kompass.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionOptionKey
import platform.AVFAudio.AVAudioSessionInterruptionOptionShouldResume
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeEnded
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.AVFAudio.AVAudioSessionRouteChangeReasonKey
import platform.AVFAudio.AVAudioSessionRouteChangeReasonOldDeviceUnavailable
import platform.AVFAudio.setActive
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSDate
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
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
    private var itemObservers: List<Any> = emptyList()

    /** Set when an interruption (call, Siri) paused us while playing; resume only within 30s. */
    private var resumeAfterInterruptionUntil: Double? = null

    init {
        val center = NSNotificationCenter.defaultCenter
        center.addObserverForName(AVAudioSessionInterruptionNotification, null, NSOperationQueue.mainQueue) { note ->
            handleInterruption(note)
        }
        center.addObserverForName(AVAudioSessionRouteChangeNotification, null, NSOperationQueue.mainQueue) { note ->
            val reason = (note?.userInfo?.get(AVAudioSessionRouteChangeReasonKey) as? NSNumber)?.unsignedLongValue
            // Headphones unplugged / Bluetooth gone: pause instead of switching to the speaker.
            if (reason == AVAudioSessionRouteChangeReasonOldDeviceUnavailable && state.value.isPlaying) pause()
        }
    }

    override val playbackState: StateFlow<AudioGuidePlaybackState> = state

    override fun prepare(url: String, title: String, subtitle: String) {
        if (url.isBlank()) return

        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)

        val item = NSURL.URLWithString(url)?.let { AVPlayerItem(it) }
        if (item == null) {
            state.value = state.value.copy(error = "Audio could not be loaded.")
            return
        }

        player.replaceCurrentItemWithPlayerItem(item)
        observeItem(item)
        resumeAfterInterruptionUntil = null
        state.value = AudioGuidePlaybackState(
            sourceUrl = url,
            title = title,
            subtitle = subtitle,
            isBuffering = true,
            isPlaying = false
        )
        configureRemoteCommandsIfNeeded()
        installTimeObserver()
        player.play()
        updateNowPlayingInfo()
    }

    override fun togglePlayPause() {
        resumeAfterInterruptionUntil = null
        if (state.value.isPlaying) pause() else resume()
    }

    private fun pause() {
        player.pause()
        state.value = state.value.copy(isPlaying = false)
        updateNowPlayingInfo()
    }

    private fun resume() {
        player.play()
        state.value = state.value.copy(isPlaying = true, isCompleted = false)
        updateNowPlayingInfo()
    }

    private fun handleInterruption(note: NSNotification?) {
        val info = note?.userInfo ?: return
        when ((info[AVAudioSessionInterruptionTypeKey] as? NSNumber)?.unsignedLongValue) {
            AVAudioSessionInterruptionTypeBegan -> if (state.value.isPlaying) {
                pause()
                resumeAfterInterruptionUntil = now() + RESUME_WINDOW_SECONDS
            }
            AVAudioSessionInterruptionTypeEnded -> {
                val options = (info[AVAudioSessionInterruptionOptionKey] as? NSNumber)?.unsignedLongValue ?: 0uL
                val deadline = resumeAfterInterruptionUntil
                resumeAfterInterruptionUntil = null
                val shouldResume = options and AVAudioSessionInterruptionOptionShouldResume != 0uL
                if (shouldResume && deadline != null && now() <= deadline && state.value.sourceUrl != null) resume()
            }
        }
    }

    private fun observeItem(item: AVPlayerItem) {
        val center = NSNotificationCenter.defaultCenter
        itemObservers.forEach(center::removeObserver)
        itemObservers = listOf(
            center.addObserverForName(AVPlayerItemDidPlayToEndTimeNotification, item, NSOperationQueue.mainQueue) { _ ->
                state.value = state.value.copy(isPlaying = false, isBuffering = false, isCompleted = true)
                updateNowPlayingInfo()
            },
            center.addObserverForName(AVPlayerItemFailedToPlayToEndTimeNotification, item, NSOperationQueue.mainQueue) { _ ->
                state.value = state.value.copy(isPlaying = false, isBuffering = false, error = "Audio could not be played.")
            }
        )
    }

    private fun now(): Double = NSDate().timeIntervalSince1970

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
        resumeAfterInterruptionUntil = null
        NSNotificationCenter.defaultCenter.let { center -> itemObservers.forEach(center::removeObserver) }
        itemObservers = emptyList()
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
            interval = CMTimeMakeWithSeconds(0.5, preferredTimescale = 600),
            queue = null
        ) { time ->
            val currentMs = (CMTimeGetSeconds(time) * 1000.0).roundToLong()
            val durationSeconds = player.currentItem?.duration?.let { CMTimeGetSeconds(it) } ?: 0.0
            val durationMs = (durationSeconds * 1000.0).roundToLong().coerceAtLeast(0L)

            val item = player.currentItem
            if (item?.status == AVPlayerItemStatusFailed) {
                state.value = state.value.copy(isPlaying = false, isBuffering = false, error = "Audio could not be played.")
                return@addPeriodicTimeObserverForInterval
            }
            if (state.value.isCompleted) return@addPeriodicTimeObserverForInterval
            state.value = state.value.copy(
                progressMs = currentMs,
                durationMs = durationMs,
                isPlaying = player.rate > 0.0f,
                isBuffering = player.timeControlStatus == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
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

    private companion object {
        const val RESUME_WINDOW_SECONDS = 30.0
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
