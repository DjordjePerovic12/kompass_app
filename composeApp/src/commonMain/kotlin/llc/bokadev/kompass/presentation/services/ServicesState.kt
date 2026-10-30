package llc.bokadev.kompass.presentation.services

import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

data class ServicesState(
    override val isLoading: Boolean = true,
    /** Nothing could be loaded (no services on screen). */
    override val error: BaseError? = null,
    val services: List<Service> = emptyList(),
    /** A retry failed while earlier services are still shown. */
    val refreshFailed: Boolean = false
) : BaseViewState(isLoading, error)
