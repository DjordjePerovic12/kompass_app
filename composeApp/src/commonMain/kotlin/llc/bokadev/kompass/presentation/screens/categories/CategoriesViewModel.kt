package llc.bokadev.kompass.presentation.screens.categories

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CategoriesState(
    val isLoading: Boolean = false
)

sealed interface CategoriesIntent

sealed interface CategoriesSideEffect

class CategoriesViewModel : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    fun onIntent(intent: CategoriesIntent) {
        // Handle intents
    }
}
