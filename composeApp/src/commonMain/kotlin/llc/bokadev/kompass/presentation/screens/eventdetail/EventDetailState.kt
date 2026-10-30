package llc.bokadev.kompass.presentation.screens.eventdetail

import llc.bokadev.kompass.core.presentation.mvi.BaseViewState
import llc.bokadev.kompass.core.presentation.mvi.UiText
import llc.bokadev.kompass.domain.model.Event

data class EventDetailState(
    override val isLoading: Boolean = true,
    override val error: UiText? = null,
    val event: Event? = null
) : BaseViewState
