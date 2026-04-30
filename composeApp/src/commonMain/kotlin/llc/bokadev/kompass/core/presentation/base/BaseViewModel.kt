package llc.bokadev.kompass.core.presentation.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel<S : BaseState, E : BaseEvent> : ViewModel() {

    protected abstract val initialState: S

    protected val _state by lazy { MutableStateFlow(initialState) }
    val state: StateFlow<S> by lazy { _state }

    protected val _sideEffectChannel = Channel<String>()
    val sideEffectFlow = _sideEffectChannel.receiveAsFlow()

    abstract fun onIntent(event: E)
}
