package llc.bokadev.kompass.presentation.screens.category_items_list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.usecase.GetPlacesByCategoryUseCase

class CategoryItemsListViewModel(
    private val getPlacesByCategory: GetPlacesByCategoryUseCase
) : BaseViewModel<CategoryItemsListState, CategoryItemsListEvent>() {

    override val initialState = CategoryItemsListState()

    fun init(categoryName: String) {
        val category = runCatching { PlaceCategory.valueOf(categoryName) }
            .getOrDefault(PlaceCategory.SEE_AND_VISIT)
        _state.update { it.copy(selectedCategory = category) }
        loadPlaces(category)
    }

    override fun onIntent(event: CategoryItemsListEvent) {
        when (event) {
            is CategoryItemsListEvent.SelectCategory -> {
                _state.update { it.copy(selectedCategory = event.category) }
                loadPlaces(event.category)
            }
            CategoryItemsListEvent.LoadPlaces -> loadPlaces(_state.value.selectedCategory)
        }
    }

    private fun loadPlaces(category: PlaceCategory) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getPlacesByCategory(category)
                .onSuccess { places -> _state.update { it.copy(isLoading = false, places = places) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.message) } }
        }
    }
}
