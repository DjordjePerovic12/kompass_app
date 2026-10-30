package llc.bokadev.kompass.presentation.screens.experiencedetail

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed interface ExperienceDetailEvent : BaseEvent {
    data class Launch(val launch: ExternalLaunch) : ExperienceDetailEvent
}
