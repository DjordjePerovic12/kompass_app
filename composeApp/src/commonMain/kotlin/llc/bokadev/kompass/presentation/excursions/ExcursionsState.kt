package llc.bokadev.kompass.presentation.excursions

import llc.bokadev.kompass.domain.model.Excursion
import llc.bokadev.kompass.domain.model.ExcursionFilter
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/** [isLoading]: nothing to show yet. [error]: nothing usable could be loaded. */
data class ExcursionsState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val items: List<Excursion> = emptyList(),
    val filter: ExcursionFilter = ExcursionFilter(),
    /** Showing a saved copy of the collection (offline/stale). */
    val cached: Boolean = false,
    val savedKeys: Set<FavoriteKey> = emptySet()
) : BaseViewState(isLoading, error)
