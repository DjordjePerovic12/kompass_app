package llc.bokadev.kompass.presentation.listen

import llc.bokadev.kompass.domain.model.AudioSource
import llc.bokadev.kompass.presentation.base.BaseEvent

/**
 * Library intents. Playback (play/pause, stop, seek, skip) and the remove dialog's visibility are
 * handled by the Screen, which owns this library's playback key, so they have no members here.
 */
sealed class ListenEvent : BaseEvent {
    data object NavigateBack : ListenEvent()
    data object OnRetryClick : ListenEvent()
    data class OnViewSelect(val view: ListenView) : ListenEvent()

    /** Not downloaded → start; downloading → cancel; downloaded → ask to remove. */
    data class OnDownloadClick(val item: ListenTrack) : ListenEvent()
    data class OnRemoveConfirm(val item: ListenTrack) : ListenEvent()

    /** A row or the Now playing card: the item's own detail (place, activity or story). */
    data class OnTrackClick(val source: AudioSource, val id: String) : ListenEvent()

    /** Emitted by the ViewModel on eventFlow: the "Download failed" snackbar. */
    data object ShowDownloadFailed : ListenEvent()

    /** Emitted by the ViewModel on eventFlow: a downloaded track's bookmark was tapped; confirm before removing. */
    data class ConfirmRemoval(val trackId: String) : ListenEvent()
}
