package llc.bokadev.kompass.presentation.screens.listen

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent

sealed interface ListenEvent : BaseEvent {
    data object DownloadFailed : ListenEvent
    /** A downloaded track's bookmark was tapped: confirm before removing the file. */
    data class ConfirmRemoval(val trackId: String) : ListenEvent
}
