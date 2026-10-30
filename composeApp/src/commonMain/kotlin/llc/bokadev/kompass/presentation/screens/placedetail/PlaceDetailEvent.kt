package llc.bokadev.kompass.presentation.screens.placedetail

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

/** One-off UI effects; navigation goes through the Navigator. */
sealed interface PlaceDetailEvent : BaseEvent {
    data class Launch(val launch: ExternalLaunch) : PlaceDetailEvent
}
