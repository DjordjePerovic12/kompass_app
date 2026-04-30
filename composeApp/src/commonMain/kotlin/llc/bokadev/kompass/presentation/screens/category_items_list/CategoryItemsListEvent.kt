package llc.bokadev.kompass.presentation.screens.category_items_list

import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.domain.model.PlaceCategory

sealed interface CategoryItemsListEvent : BaseEvent {
    data class SelectCategory(val category: PlaceCategory) : CategoryItemsListEvent
    data object LoadPlaces : CategoryItemsListEvent
}
