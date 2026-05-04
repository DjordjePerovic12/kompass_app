package llc.bokadev.kompass.core.util

import kotlinx.coroutines.flow.StateFlow

data class AudioGuidePlaybackState(
    val sourceUrl: String? = null,
    val title: String = "",
    val subtitle: String = "",
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val durationMs: Long = 0L,
    val progressMs: Long = 0L,
    val error: String? = null
)

interface AudioGuidePlayer {
    val playbackState: StateFlow<AudioGuidePlaybackState>
    fun prepare(url: String, title: String, subtitle: String = "")
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun stop()
}
