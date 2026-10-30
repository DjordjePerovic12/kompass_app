package llc.bokadev.kompass.presentation.screens.localfinds

import llc.bokadev.kompass.core.presentation.mvi.BaseAction

sealed interface LocalFindDetailAction : BaseAction {
    data object Retry : LocalFindDetailAction
    data object Back : LocalFindDetailAction
    /** The Listen pill; handled by the screen's audio wiring (it owns the playback owner key). */
    data object Listen : LocalFindDetailAction
    data class OpenMap(val latitude: Double, val longitude: Double, val title: String) : LocalFindDetailAction
    /** Next story replaces this one, so Back returns to the list, not the previous story. */
    data class OpenNext(val id: String) : LocalFindDetailAction
    data object UserScrolled : LocalFindDetailAction
}
