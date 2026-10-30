package llc.bokadev.kompass.presentation.essentials.utility_map

import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState
import llc.bokadev.kompass.presentation.essentials.utils.UtilityLocationItem

data class UtilityMapState(
    override val isLoading: Boolean = true,
    /** Nothing could be loaded (no points on screen). */
    override val error: BaseError? = null,
    val category: UtilityCategory? = null,
    /** True for "All on map": every category's nearest utilities on one map. */
    val showAll: Boolean = false,
    val currentLocation: GeoPoint? = null,
    val items: List<UtilityLocationItem> = emptyList(),
    val selectedUtilityId: String? = null,
    /** The point the visitor had selected disappeared from the data after a reload. */
    val selectionGone: Boolean = false,
    /** A reload failed while the previously loaded points are still shown. */
    val refreshFailed: Boolean = false
) : BaseViewState(isLoading, error)
