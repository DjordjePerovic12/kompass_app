package llc.bokadev.kompass.presentation.screens.localfinds

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed interface LocalFindDetailEvent : BaseEvent {
    data class Launch(val launch: ExternalLaunch) : LocalFindDetailEvent
}
