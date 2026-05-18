package llc.bokadev.kompass.core.util

import android.content.Context
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.StateFlow
import llc.bokadev.kompass.AudioGuidePlaybackService

class AndroidAudioGuidePlayer(
    private val context: Context
) : AudioGuidePlayer {

    override val playbackState: StateFlow<AudioGuidePlaybackState> =
        AudioGuidePlaybackService.stateFlow()

    override fun prepare(url: String, title: String, subtitle: String) {
        ContextCompat.startForegroundService(
            context,
            AudioGuidePlaybackService.prepareIntent(context, url, title, subtitle)
        )
    }

    override fun togglePlayPause() {
        context.startService(AudioGuidePlaybackService.toggleIntent(context))
    }

    override fun seekTo(positionMs: Long) {
        context.startService(AudioGuidePlaybackService.seekIntent(context, positionMs))
    }

    override fun stop() {
        context.startService(AudioGuidePlaybackService.stopIntent(context))
    }
}
