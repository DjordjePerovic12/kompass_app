package llc.bokadev.kompass.presentation.screens.listen

import llc.bokadev.kompass.core.presentation.mvi.BaseAction
import llc.bokadev.kompass.domain.model.AudioSource

/**
 * Library intents. Playback actions ([Play], [ToggleCurrent], [Stop], [Seek], [Skip]) and the remove
 * confirmation are handled by the screen's orchestrator, which owns this library's playback key.
 */
sealed interface ListenAction : BaseAction {
    data class SelectView(val view: ListenView) : ListenAction
    /** Not downloaded → start; downloading → cancel; downloaded → ask to remove. */
    data class Download(val item: ListenTrack) : ListenAction
    data class ConfirmRemove(val item: ListenTrack) : ListenAction
    data object DismissRemove : ListenAction
    /** A row or the Now playing card: the item's own detail (place, activity or story). */
    data class Open(val source: AudioSource, val id: String) : ListenAction
    data object OpenCurrent : ListenAction
    data class Play(val item: ListenTrack) : ListenAction
    data object ToggleCurrent : ListenAction
    data object Stop : ListenAction
    data class Seek(val positionMs: Long) : ListenAction
    data class Skip(val deltaMs: Long) : ListenAction
    data object Retry : ListenAction
    data object Back : ListenAction
}
