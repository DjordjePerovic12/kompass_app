package llc.bokadev.kompass.presentation.screens.infocenter

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed interface InfoCenterDetailEvent : BaseEvent {
    data class Launch(val launch: ExternalLaunch) : InfoCenterDetailEvent
}
