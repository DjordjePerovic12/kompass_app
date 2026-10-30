package llc.bokadev.kompass.presentation.screens.excursions

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.Excursion
import llc.bokadev.kompass.domain.model.ExcursionFilter
import llc.bokadev.kompass.domain.model.FavoriteKey

data class ExcursionsState(
    val items: List<Excursion> = emptyList(),
    val filter: ExcursionFilter = ExcursionFilter(),
    /** Showing a saved copy of the collection (offline/stale). */
    val cached: Boolean = false,
    val savedKeys: Set<FavoriteKey> = emptySet(),
    override val isLoading: Boolean = true,
    override val error: UiText? = null
) : BaseViewState
