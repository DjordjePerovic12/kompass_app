package llc.bokadev.kompass.presentation.screens.localfinds

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase

class LocalFindsViewModel(
    private val getLocalFinds: GetLocalFindsUseCase
) : BaseViewModel<LocalFindsState, LocalFindsIntent>() {

    override val initialState = LocalFindsState()

    init {
        load()
    }

    override fun onIntent(event: LocalFindsIntent) {
        when (event) {
            LocalFindsIntent.Load -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getLocalFinds()
                .onSuccess { items -> _state.update { it.copy(isLoading = false, items = items) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }
}
