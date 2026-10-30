package llc.bokadev.kompass.presentation.screens.services

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed interface ServiceDetailEvent : BaseEvent {
    data class Launch(val launch: ExternalLaunch) : ServiceDetailEvent
}
