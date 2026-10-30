package llc.bokadev.kompass.presentation.excursions.related_activities

import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/**
 * The activities linked to a place, shown inside the place detail.
 * [isLoading]: nothing to show yet. [error]: the relation couldn't be loaded; the section offers a retry.
 */
data class RelatedActivitiesState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val items: List<Experience> = emptyList(),
    val cached: Boolean = false,
    val savedActivityIds: Set<String> = emptySet()
) : BaseViewState(isLoading, error)
