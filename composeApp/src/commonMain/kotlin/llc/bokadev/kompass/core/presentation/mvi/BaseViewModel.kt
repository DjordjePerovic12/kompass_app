package llc.bokadev.kompass.core.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Every screen's ViewModel: [Action] is the sealed user-intent type dispatched through
 * [onAction], [Event] is the sealed one-off UI-effect type delivered through [events].
 * Navigation is never an [Event] — ViewModels that navigate take a [Navigator] constructor param
 * and call it directly. The initial load belongs in [onFirstStart].
 */
abstract class BaseViewModel<State : Any, Action : BaseAction, Event : BaseEvent>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    init {
        // viewModelScope's default dispatcher is Dispatchers.Main.immediate, which runs this
        // launch block synchronously inline when called from the main thread — i.e. *during*
        // this constructor, before a subclass's own property initializers (which run after a
        // superclass's init block, per Kotlin/JVM construction order) have executed. Forcing
        // Dispatchers.Main here (not .immediate) makes it a real post, so onFirstStart() only
        // runs once construction — including the concrete subclass — has fully completed.
        viewModelScope.launch(Dispatchers.Main) { onFirstStart() }
    }

    /** Runs once, right after construction — the place for a ViewModel's initial data load. */
    protected open suspend fun onFirstStart() = Unit

    abstract fun onAction(action: Action)

    protected fun updateState(update: (State) -> State) {
        _state.value = update(_state.value)
    }

    protected fun sendEvent(event: Event) {
        _events.trySend(event)
    }

    /** Exposed for subclasses that need a lifecycle-scoped, multi-subscriber hot flow. */
    protected fun <T> Flow<T>.stateInViewModel(initialValue: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), initialValue)
}
