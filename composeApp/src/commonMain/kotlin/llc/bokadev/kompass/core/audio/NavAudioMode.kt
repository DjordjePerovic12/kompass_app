package llc.bokadev.kompass.core.audio

/** What the floating bar's center button shows and does (place detail spec §2.1–2.2). */
sealed interface NavAudioMode {
    /** No place audio on this screen: headphones icon, tap opens the audio library. */
    data object NoAudio : NavAudioMode
    data class Ready(val track: AudioTrack) : NavAudioMode
    data class Loading(val track: AudioTrack) : NavAudioMode
    data class Playing(val track: AudioTrack, val positionMs: Long, val durationMs: Long?) : NavAudioMode
    data class Paused(val track: AudioTrack, val positionMs: Long, val durationMs: Long?) : NavAudioMode
    data class Failed(val track: AudioTrack) : NavAudioMode
    /** Offline and the track isn't downloaded: dimmed, tap explains. */
    data class Unavailable(val track: AudioTrack) : NavAudioMode
}

/** 0..1, or null when the duration isn't known (no progress ring then). */
val NavAudioMode.progress: Float?
    get() = when (this) {
        is NavAudioMode.Playing -> fraction(positionMs, durationMs)
        is NavAudioMode.Paused -> fraction(positionMs, durationMs)
        else -> null
    }

private fun fraction(positionMs: Long, durationMs: Long?): Float? =
    durationMs?.takeIf { it > 0 }?.let { (positionMs.toFloat() / it).coerceIn(0f, 1f) }

/**
 * Playback state only belongs to [context] when the track ids match: audio playing for another
 * place (or none at all) shows this screen's track as Ready. Offline wins over everything except
 * a track that is already playing or paused from the local file.
 */
fun resolveNavAudioMode(context: NavAudioContext?, state: AudioState, offline: Boolean): NavAudioMode {
    val track = context?.track ?: return NavAudioMode.NoAudio
    val sameTrack = when (state) {
        is AudioState.Loading -> state.track.id == track.id
        is AudioState.Playing -> state.track.id == track.id
        is AudioState.Paused -> state.track.id == track.id
        is AudioState.Failed -> state.track.id == track.id
        AudioState.Idle -> false
    }
    return when {
        sameTrack && state is AudioState.Playing -> NavAudioMode.Playing(track, state.positionMs, state.durationMs)
        sameTrack && state is AudioState.Paused -> NavAudioMode.Paused(track, state.positionMs, state.durationMs)
        offline && !track.isDownloaded -> NavAudioMode.Unavailable(track)
        sameTrack && state is AudioState.Loading -> NavAudioMode.Loading(track)
        sameTrack && state is AudioState.Failed -> NavAudioMode.Failed(track)
        else -> NavAudioMode.Ready(track)
    }
}
