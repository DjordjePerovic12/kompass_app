package llc.bokadev.kompass.presentation.screens.categorylist

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.FavoriteKey

data class CategoryListState(
    override val isLoading: Boolean = true,
    /** Nothing could be loaded (no rows on screen). */
    override val error: UiText? = null,
    val kind: CategoryListKind = CategoryListKind.PLACES,
    /**
     * Everything in the current filter, by proximity when location is known, else editorial order.
     * Saving never reorders it: a saved item stays where the visitor tapped it.
     */
    val items: List<ListItem> = emptyList(),
    val savedKeys: Set<FavoriteKey> = emptySet(),
    val areas: List<String> = emptyList(),
    val areaFilter: String? = null,
    /** True when showing previous data after a failed refresh. */
    val refreshFailed: Boolean = false
) : BaseViewState {
    val count: Int get() = items.size
    fun isSaved(item: ListItem): Boolean = FavoriteKey(item.type, item.id) in savedKeys
}
