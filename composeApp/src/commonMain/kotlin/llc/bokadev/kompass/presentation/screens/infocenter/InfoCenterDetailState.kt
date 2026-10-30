package llc.bokadev.kompass.presentation.screens.infocenter

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.InfoNotice

data class InfoCenterDetailState(
    override val isLoading: Boolean = true,
    override val error: UiText? = null,
    val notice: InfoNotice? = null
) : BaseViewState
