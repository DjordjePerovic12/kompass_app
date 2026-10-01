package llc.bokadev.kompass.presentation.screens.localfinds

import llc.bokadev.kompass.core.presentation.base.BaseEvent

sealed interface LocalFindsIntent : BaseEvent {
    data object Load : LocalFindsIntent
}
