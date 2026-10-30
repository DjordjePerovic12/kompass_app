package llc.bokadev.kompass.presentation.screens.services

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.Service

data class ServicesState(
    override val isLoading: Boolean = true,
    /** Nothing could be loaded (no services on screen). */
    override val error: UiText? = null,
    val services: List<Service> = emptyList(),
    /** A retry failed while earlier services are still shown. */
    val refreshFailed: Boolean = false
) : BaseViewState
