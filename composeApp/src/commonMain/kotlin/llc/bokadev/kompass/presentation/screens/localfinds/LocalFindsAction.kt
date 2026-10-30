package llc.bokadev.kompass.presentation.screens.localfinds

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface LocalFindsAction : BaseAction {
    data object Load : LocalFindsAction
    data class SelectFilter(val kind: StoryKind?) : LocalFindsAction
    /** Opens a story; the current filter goes along so "Next story" follows the same list. */
    data class Open(val id: String) : LocalFindsAction
    data object Back : LocalFindsAction
}
