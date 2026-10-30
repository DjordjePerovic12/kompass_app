package llc.bokadev.kompass.presentation.localfinds.detail

import llc.bokadev.kompass.presentation.base.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed class LocalFindDetailEvent : BaseEvent {
    data object NavigateBack : LocalFindDetailEvent()
    data object OnRetryClick : LocalFindDetailEvent()
    data class OnMapClick(val latitude: Double, val longitude: Double, val title: String) : LocalFindDetailEvent()

    /** Next story replaces this one, so Back returns to the list, not the previous story. */
    data class OnNextStoryClick(val id: String) : LocalFindDetailEvent()
    data object OnUserScrolled : LocalFindDetailEvent()

    /** Emitted by the ViewModel on eventFlow: hands off to a maps app. */
    data class OpenExternal(val launch: ExternalLaunch) : LocalFindDetailEvent()
}
