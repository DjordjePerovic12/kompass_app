package llc.bokadev.kompass.presentation.screens.infocenter

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.InfoNotice

data class InfoCenterState(
    override val isLoading: Boolean = true,
    /** Nothing could be loaded (no notices on screen). */
    override val error: UiText? = null,
    val notices: List<InfoNotice> = emptyList(),
    /** A retry failed while earlier notices are still shown. */
    val refreshFailed: Boolean = false
) : BaseViewState
