package llc.bokadev.kompass.presentation.screens.eventdetail

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

/** One-off UI effects; navigation goes through the Navigator. */
sealed interface EventDetailEvent : BaseEvent {
    data class Launch(val launch: ExternalLaunch) : EventDetailEvent
}
