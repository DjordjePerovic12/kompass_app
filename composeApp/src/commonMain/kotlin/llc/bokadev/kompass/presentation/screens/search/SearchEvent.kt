package llc.bokadev.kompass.presentation.screens.search

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent

sealed interface SearchEvent : BaseEvent {
    /** Leaving for a result: put the keyboard away first. */
    data object HideKeyboard : SearchEvent
}
