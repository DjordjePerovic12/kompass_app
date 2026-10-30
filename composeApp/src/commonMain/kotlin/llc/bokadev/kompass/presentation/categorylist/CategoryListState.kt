package llc.bokadev.kompass.presentation.categorylist

import llc.bokadev.kompass.domain.model.CategoryListKind
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/**
 * [isLoading]: nothing to show yet. [error]: nothing could be loaded (no rows on screen).
 * [refreshFailed]: showing previous data after a failed refresh.
 */
data class CategoryListState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val kind: CategoryListKind = CategoryListKind.PLACES,
    /**
     * Everything in the current filter, by proximity when location is known, else editorial order.
     * Saving never reorders it: a saved item stays where the visitor tapped it.
     */
    val items: List<ListItem> = emptyList(),
    val savedKeys: Set<FavoriteKey> = emptySet(),
    val areas: List<String> = emptyList(),
    val areaFilter: String? = null,
    val refreshFailed: Boolean = false
) : BaseViewState(isLoading, error) {
    val count: Int get() = items.size
    fun isSaved(item: ListItem): Boolean = FavoriteKey(item.type, item.id) in savedKeys
}

/** One row, already resolved. [distanceKm] only comes from the device's real location. */
data class ListItem(
    val type: FavoriteItemType,
    val id: String,
    val place: Place? = null,
    val activity: Experience? = null,
    val areaKey: String,
    val distanceKm: Double?,
    val sortOrder: Int
)
