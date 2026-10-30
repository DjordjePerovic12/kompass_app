package llc.bokadev.kompass.presentation.eventdetail

import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/** [isLoading]: nothing to show yet. [error]: nothing usable could be loaded. */
data class EventDetailState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val event: Event? = null
) : BaseViewState(isLoading, error)
