package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_audio_unavailable_offline
import kompass.composeapp.generated.resources.detail_english_audio
import kompass.composeapp.generated.resources.detail_listen_audio_guide
import kompass.composeapp.generated.resources.detail_listen_audio_guide_duration
import kompass.composeapp.generated.resources.detail_loading_audio
import kompass.composeapp.generated.resources.detail_pause_audio
import kompass.composeapp.generated.resources.detail_resume_audio
import kompass.composeapp.generated.resources.detail_retry_audio
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.audio.NavAudioMode
import llc.bokadev.kompass.core.utils.toClockDuration
import llc.bokadev.kompass.domain.model.AudioSource
import org.jetbrains.compose.resources.stringResource

/**
 * "Listen to the audio guide" on place and activity detail: the explicit play control for the
 * screen's free audio (the center button only opens the library). Says "English audio" when the
 * app language has no track, and is dimmed offline when the track isn't downloaded.
 */
@Composable
fun DetailListenControl(
    mode: NavAudioMode,
    reduceMotion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = mode.trackOrNull()
    val duration = track?.durationMs?.toClockDuration()
    val base = if (duration != null) {
        stringResource(Res.string.detail_listen_audio_guide_duration, duration)
    } else {
        stringResource(Res.string.detail_listen_audio_guide)
    }
    val englishAudio = stringResource(Res.string.detail_english_audio)
    val label = if (track?.isEnglishFallback == true) "$base · $englishAudio" else base
    ListenPill(
        label = label,
        disc = mode.toDisc(),
        unavailable = mode is NavAudioMode.Unavailable,
        description = mode.description(label),
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

@Composable
private fun NavAudioMode.description(label: String): String = when (this) {
    is NavAudioMode.Loading -> stringResource(Res.string.detail_loading_audio)
    is NavAudioMode.Playing -> stringResource(
        Res.string.detail_pause_audio,
        positionMs.toClockDuration() ?: "0:00",
        durationMs?.toClockDuration() ?: ""
    )
    is NavAudioMode.Paused -> stringResource(Res.string.detail_resume_audio, positionMs.toClockDuration() ?: "0:00")
    is NavAudioMode.Failed -> stringResource(Res.string.detail_retry_audio)
    is NavAudioMode.Unavailable -> stringResource(Res.string.detail_audio_unavailable_offline)
    else -> label
}

@Preview
@Composable
private fun DetailListenControlPreview() {
    val track = AudioTrack(AudioSource.PLACE, "p", "Saint Tryphon", "Old Town", "a.mp3", "en", isEnglishFallback = true, isDownloaded = false)
    DetailListenControl(NavAudioMode.Ready(track), reduceMotion = true, onClick = {})
}
