package llc.bokadev.kompass.presentation.screens.categories

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.usecase.GetCategoriesUseCase

sealed interface CategoriesEvent : BaseEvent {
    data object Retry : CategoriesEvent
}

class CategoriesViewModel(
    private val getCategories: GetCategoriesUseCase
) : BaseViewModel<CategoriesState, CategoriesEvent>() {

    override val initialState = CategoriesState()

    init { loadCategories() }

    override fun onIntent(event: CategoriesEvent) {
        when (event) {
            CategoriesEvent.Retry -> loadCategories()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getCategories()
                .onSuccess { cats -> _state.update { it.copy(isLoading = false, categories = cats) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }
}
