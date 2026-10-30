package llc.bokadev.kompass.presentation.infocenter.detail

import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.presentation.base.BaseError
import llc.bokadev.kompass.presentation.base.BaseViewState

/** [isLoading]: nothing to show yet. [error]: nothing usable could be loaded. */
data class InfoCenterDetailState(
    override val isLoading: Boolean = true,
    override val error: BaseError? = null,
    val notice: InfoNotice? = null
) : BaseViewState(isLoading, error)
