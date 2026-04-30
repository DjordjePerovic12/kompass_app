package llc.bokadev.kompass.presentation.screens.essentials

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.usecase.GetEssentialsUseCase

data class EssentialsState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val groupedEssentials: Map<EssentialCategory, List<CityEssential>> = emptyMap(),
    val expandedIds: Set<String> = emptySet()
) : BaseState()

sealed interface EssentialsEvent : BaseEvent {
    data class ToggleItem(val id: String) : EssentialsEvent
    data object Retry : EssentialsEvent
}

class EssentialsViewModel(
    private val getEssentials: GetEssentialsUseCase
) : BaseViewModel<EssentialsState, EssentialsEvent>() {

    override val initialState = EssentialsState()

    init { load() }

    override fun onIntent(event: EssentialsEvent) {
        when (event) {
            is EssentialsEvent.ToggleItem -> toggleItem(event.id)
            EssentialsEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getEssentials()
                .onSuccess { essentials ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            groupedEssentials = essentials.groupBy { e -> e.category }
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }

    private fun toggleItem(id: String) {
        _state.update {
            val newExpanded = if (id in it.expandedIds) it.expandedIds - id else it.expandedIds + id
            it.copy(expandedIds = newExpanded)
        }
    }
}
