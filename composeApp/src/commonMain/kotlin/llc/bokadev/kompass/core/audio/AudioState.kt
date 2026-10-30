package llc.bokadev.kompass.core.audio

import llc.bokadev.kompass.domain.model.AudioSource
import llc.bokadev.kompass.domain.model.chooseAudio

/**
 * One free audio track (a place, activity or story) in one language. [audioPath] is the CMS
 * storage path; it is resolved to a playable (signed remote or cached local) URL only when
 * playback actually starts.
 */
data class AudioTrack(
    val source: AudioSource,
    val sourceId: String,
    val title: String,
    val subtitle: String,
    val audioPath: String,
    val lang: String,
    /** True when the app language had no track and this is the English fallback. */
    val isEnglishFallback: Boolean,
    /** True when the file is already in the offline audio store and plays without network. */
    val isDownloaded: Boolean,
    val durationMs: Long? = null
) {
    /** Stable per source type + id + language: a place and a story never share a track. */
    val id: String get() = "${source.name.lowercase()}:$sourceId:$lang"

    companion object {
        /** The track for [files] in [lang] under the shared language rule, or null without audio. */
        fun of(
            source: AudioSource,
            sourceId: String,
            title: String,
            subtitle: String,
            files: Map<String, String>?,
            lang: String,
            isDownloaded: (path: String) -> Boolean
        ): AudioTrack? {
            val choice = chooseAudio(files, lang) ?: return null
            return AudioTrack(
                source = source, sourceId = sourceId, title = title, subtitle = subtitle, audioPath = choice.path,
                lang = choice.lang, isEnglishFallback = choice.isEnglishFallback, isDownloaded = isDownloaded(choice.path)
            )
        }
    }
}

/** The single app-wide playback state for audio started from the floating bar's center button. */
sealed interface AudioState {
    /** The loaded track, if any. */
    val trackOrNull: AudioTrack?
        get() = when (this) {
            Idle -> null
            is Loading -> track
            is Playing -> track
            is Paused -> track
            is Failed -> track
        }

    data object Idle : AudioState
    data class Loading(val track: AudioTrack, val requestId: Long) : AudioState
    data class Playing(val track: AudioTrack, val positionMs: Long, val durationMs: Long?) : AudioState
    data class Paused(val track: AudioTrack, val positionMs: Long, val durationMs: Long?) : AudioState
    data class Failed(val track: AudioTrack, val reason: Reason) : AudioState

    enum class Reason { Network, Decode, Unknown }
}

/**
 * Published by the top-most screen: "the center button should control this track right now".
 * A null [track] means the screen has no place audio, so the button opens the audio library.
 */
data class NavAudioContext(
    val ownerKey: String,
    val track: AudioTrack?,
    /** The audio library itself is on top: the button shows a "you're here" ring and does nothing. */
    val isLibrary: Boolean = false
)
