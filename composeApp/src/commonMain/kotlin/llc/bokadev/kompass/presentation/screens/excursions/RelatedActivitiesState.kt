package llc.bokadev.kompass.presentation.screens.excursions

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.Experience

/** The activities linked to a place, shown inside the place detail. */
data class RelatedActivitiesState(
    val items: List<Experience> = emptyList(),
    val cached: Boolean = false,
    val savedActivityIds: Set<String> = emptySet(),
    override val isLoading: Boolean = false,
    /** The relation couldn't be loaded; the section offers a retry. */
    override val error: UiText? = null
) : BaseViewState
