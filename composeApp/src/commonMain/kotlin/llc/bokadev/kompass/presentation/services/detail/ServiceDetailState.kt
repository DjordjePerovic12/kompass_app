package llc.bokadev.kompass.presentation.services.detail

import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/** [isLoading]: nothing to show yet. [error]: nothing usable could be loaded. */
data class ServiceDetailState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val service: Service? = null
) : BaseViewState(isLoading, error)
