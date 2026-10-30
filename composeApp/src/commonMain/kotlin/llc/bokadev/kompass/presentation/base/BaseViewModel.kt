package llc.bokadev.kompass.presentation.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
  A base ViewModel class providing common state management for UI components.

  This abstract class is designed to be extended by feature-specific ViewModels.
  It manages UI state using [StateFlow] and event communication using [SharedFlow].
  It also provides a mechanism for handling transient UI messages (e.g., snackbars).

  @param S The type of [BaseViewState] that represents the UI state.
  @param E The type of [BaseEvent] that represents UI events.

  ## Features:
  - Holds the initial view state and exposes it as an observable [StateFlow].
  - Provides a `snackBarChannel` for transient UI messages.
  - Exposes an event flow for dispatching UI events.
  - Defines an abstract function `dismissError()` for handling error dismissal.

  See EventDetailViewModel for example usage
 **/

abstract class BaseViewModel<S : BaseViewState, E : BaseEvent> : ViewModel() {

    protected abstract val initialViewState: S
    protected val state by lazy { MutableStateFlow(initialViewState) }
    val viewStateFlow: StateFlow<S> by lazy { state }

    protected val _snackBarChannel = Channel<String>()
    val snackBarChannel = _snackBarChannel.receiveAsFlow()

    protected val event = MutableSharedFlow<E>()
    val eventFlow: SharedFlow<E> = event

    abstract fun dismissError()
}
