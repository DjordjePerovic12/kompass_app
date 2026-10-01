package llc.bokadev.kompass.presentation.screens.localfinds

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.usecase.GetLocalFindByIdUseCase
import llc.bokadev.kompass.domain.usecase.HasPremiumAccessUseCase

data class LocalFindDetailState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val find: LocalFind? = null,
    val hasDeepAccess: Boolean = false
) : BaseState()

sealed interface LocalFindDetailEvent : BaseEvent {
    data object Retry : LocalFindDetailEvent
}

class LocalFindDetailViewModel(
    private val id: String,
    private val getLocalFindById: GetLocalFindByIdUseCase,
    private val hasPremiumAccess: HasPremiumAccessUseCase
) : BaseViewModel<LocalFindDetailState, LocalFindDetailEvent>() {

    override val initialState = LocalFindDetailState()

    init {
        load()
    }

    override fun onIntent(event: LocalFindDetailEvent) {
        when (event) {
            LocalFindDetailEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getLocalFindById(id)
                .onSuccess { find ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            find = find,
                            hasDeepAccess = hasPremiumAccess("audio_pass")
                        )
                    }
                }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }
}
