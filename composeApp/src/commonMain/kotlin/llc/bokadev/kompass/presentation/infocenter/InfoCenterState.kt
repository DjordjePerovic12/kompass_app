package llc.bokadev.kompass.presentation.infocenter

import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

data class InfoCenterState(
    override val isLoading: Boolean = true,
    /** Nothing could be loaded (no notices on screen). */
    override val error: BaseError? = null,
    val notices: List<InfoNotice> = emptyList(),
    /** A retry failed while earlier notices are still shown. */
    val refreshFailed: Boolean = false
) : BaseViewState(isLoading, error)
