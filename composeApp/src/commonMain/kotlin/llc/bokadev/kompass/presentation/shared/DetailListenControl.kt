package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.audio.NavAudioMode
import llc.bokadev.kompass.core.util.DetailStrings
import llc.bokadev.kompass.core.util.toClockDuration
import llc.bokadev.kompass.domain.model.AudioSource

/**
 * "Listen to the audio guide" on place and activity detail: the explicit play control for the
 * screen's free audio (the center button only opens the library). Says "English audio" when the
 * app language has no track, and is dimmed offline when the track isn't downloaded.
 */
@Composable
fun DetailListenControl(
    mode: NavAudioMode,
    strings: DetailStrings,
    reduceMotion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = mode.trackOrNull()
    val duration = track?.durationMs?.toClockDuration()
    val base = duration?.let { strings.listenAudioGuideDuration.replace("{d}", it) } ?: strings.listenAudioGuide
    val label = if (track?.isEnglishFallback == true) "$base · ${strings.englishAudio}" else base
    ListenPill(
        label = label,
        disc = mode.toDisc(),
        unavailable = mode is NavAudioMode.Unavailable,
        description = mode.description(strings, label),
        reduceMotion = reduceMotion,
        onClick = onClick,
        modifier = modifier
    )
}

private fun NavAudioMode.trackOrNull(): AudioTrack? = when (this) {
    NavAudioMode.NoAudio -> null
    is NavAudioMode.Ready -> track
    is NavAudioMode.Loading -> track
    is NavAudioMode.Playing -> track
    is NavAudioMode.Paused -> track
    is NavAudioMode.Failed -> track
    is NavAudioMode.Unavailable -> track
}

private fun NavAudioMode.description(strings: DetailStrings, label: String): String = when (this) {
    is NavAudioMode.Loading -> strings.loadingAudio
    is NavAudioMode.Playing -> strings.pauseAudio
        .replace("{elapsed}", positionMs.toClockDuration() ?: "0:00")
        .replace("{total}", durationMs?.toClockDuration() ?: "")
    is NavAudioMode.Paused -> strings.resumeAudio.replace("{elapsed}", positionMs.toClockDuration() ?: "0:00")
    is NavAudioMode.Failed -> strings.retryAudio
    is NavAudioMode.Unavailable -> strings.audioUnavailableOffline
    else -> label
}

@Preview
@Composable
private fun DetailListenControlPreview() {
    val track = AudioTrack(AudioSource.PLACE, "p", "Saint Tryphon", "Old Town", "a.mp3", "en", isEnglishFallback = true, isDownloaded = false)
    DetailListenControl(NavAudioMode.Ready(track), llc.bokadev.kompass.core.util.appStringsFor("fr").detail, reduceMotion = true, onClick = {})
}
