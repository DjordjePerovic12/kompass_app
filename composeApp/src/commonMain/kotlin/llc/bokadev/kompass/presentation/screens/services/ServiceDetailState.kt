package llc.bokadev.kompass.presentation.screens.services

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.Service

data class ServiceDetailState(
    override val isLoading: Boolean = true,
    override val error: UiText? = null,
    val service: Service? = null
) : BaseViewState
