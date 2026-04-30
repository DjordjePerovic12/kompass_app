package llc.bokadev.kompass.presentation.screens.category_items_list

import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory

data class CategoryItemsListState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val places: List<Place> = emptyList(),
    val selectedCategory: PlaceCategory = PlaceCategory.SEE_AND_VISIT
) : BaseState()
