package llc.bokadev.kompass.presentation.essentials

import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.presentation.base.BaseEvent
import llc.bokadev.kompass.presentation.shared.ExternalLaunch

sealed class EssentialsEvent : BaseEvent {
    data object NavigateBack : EssentialsEvent()
    data object OnRetryClick : EssentialsEvent()
    data object OnLanguageClick : EssentialsEvent()
    data class OnTabSelect(val tab: EssentialsTab) : EssentialsEvent()
    data class OnItemToggle(val id: String) : EssentialsEvent()

    /** Expands a module (e.g. from "How it works") without collapsing it if it's already open. */
    data class OnItemExpand(val id: String) : EssentialsEvent()
    data object OnHideReminderClick : EssentialsEvent()

    /** null [category] = "All on map"; [selectedId] pre-selects that pin. */
    data class OnUtilityMapClick(val category: UtilityCategory?, val selectedId: String?) : EssentialsEvent()
    data class OnDialClick(val number: String) : EssentialsEvent()
    data class OnLocationClick(val title: String, val latitude: Double, val longitude: Double) : EssentialsEvent()
    data class OnLinkClick(val url: String) : EssentialsEvent()
    data object OnTurnOnLocationClick : EssentialsEvent()

    /** Emitted by the ViewModel on eventFlow: hands off to the dialer, maps, browser or settings. */
    data class OpenExternal(val launch: ExternalLaunch) : EssentialsEvent()
}
