package llc.bokadev.kompass.core.presentation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** A one-off snackbar message, optionally with one action (e.g. "Undo"). */
data class SnackbarMessage(
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    /** How long it stays up; null = the default (5s with an action, short without). */
    val durationMs: Long? = null
)

/**
 * One-off messages for the app-wide snackbar host in `MainShell` (e.g. "Couldn't play audio",
 * "Removed {title} · Undo"). Any screen or the floating bar can post; the shell shows.
 */
class SnackbarBus {
    private val _messages = MutableSharedFlow<SnackbarMessage>(extraBufferCapacity = 4)
    val messages: SharedFlow<SnackbarMessage> = _messages.asSharedFlow()

    fun show(message: String) {
        _messages.tryEmit(SnackbarMessage(message))
    }

    fun show(message: SnackbarMessage) {
        _messages.tryEmit(message)
    }
}
