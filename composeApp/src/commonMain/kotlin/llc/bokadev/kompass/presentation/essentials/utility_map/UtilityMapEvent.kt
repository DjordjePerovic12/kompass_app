package llc.bokadev.kompass.presentation.essentials.utility_map

import llc.bokadev.kompass.presentation.base.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed class UtilityMapEvent : BaseEvent {
    data object NavigateBack : UtilityMapEvent()
    data object OnRetryClick : UtilityMapEvent()
    data class OnUtilitySelect(val id: String) : UtilityMapEvent()

    /** Hands the point with this id (never a name) to the system maps app. */
    data class OnNavigateClick(val id: String) : UtilityMapEvent()

    /** Emitted by the ViewModel on eventFlow: hands off to a maps app. */
    data class OpenExternal(val launch: ExternalLaunch) : UtilityMapEvent()
}
