package llc.bokadev.kompass.presentation.screens.essentials

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed interface EssentialsEvent : BaseEvent {
    data class Launch(val launch: ExternalLaunch) : EssentialsEvent
}
