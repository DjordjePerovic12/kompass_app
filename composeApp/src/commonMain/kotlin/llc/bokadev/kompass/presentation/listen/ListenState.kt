package llc.bokadev.kompass.presentation.listen

import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.domain.model.AudioCatalogEntry
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

enum class ListenView { ALL, DOWNLOADED }

sealed interface DownloadState {
    data object NotDownloaded : DownloadState
    /** [progress] is 0..1, or null while the size isn't known yet. */
    data class Downloading(val progress: Float?) : DownloadState
    data object Downloaded : DownloadState
}

/** One library row: a place's, activity's or story's free audio in the app language (English fallback). */
data class ListenTrack(
    val track: AudioTrack,
    val entry: AudioCatalogEntry,
    val areaKey: String,
    /** The item has audio in other languages too (the remove dialog says only this one goes). */
    val hasOtherLanguages: Boolean,
    val download: DownloadState
)

/** [isLoading]: nothing to show yet. [error]: nothing usable could be loaded. */
data class ListenState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val view: ListenView = ListenView.ALL,
    val tracks: List<ListenTrack> = emptyList(),
    /** Some sources (places, activities or stories) couldn't load: the library may be incomplete. */
    val isIncomplete: Boolean = false
) : BaseViewState(isLoading, error) {
    val visible: List<ListenTrack>
        get() = if (view == ListenView.DOWNLOADED) tracks.filter { it.download == DownloadState.Downloaded } else tracks
}
