package llc.bokadev.kompass.presentation.screens.experiences

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.usecase.GetActivitiesUseCase

data class ExperiencesState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val activities: List<Experience> = emptyList(),
    val selectedCategory: String? = null
) : BaseState()

sealed interface ExperiencesEvent : BaseEvent {
    data object Retry : ExperiencesEvent
    data class SelectCategory(val category: String?) : ExperiencesEvent
}

class ExperiencesViewModel(
    private val getActivities: GetActivitiesUseCase
) : BaseViewModel<ExperiencesState, ExperiencesEvent>() {

    override val initialState = ExperiencesState()

    init {
        load()
    }

    override fun onIntent(event: ExperiencesEvent) {
        when (event) {
            ExperiencesEvent.Retry -> load()
            is ExperiencesEvent.SelectCategory -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getActivities()
                .onSuccess { activities ->
                    _state.update { it.copy(isLoading = false, activities = activities) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}
