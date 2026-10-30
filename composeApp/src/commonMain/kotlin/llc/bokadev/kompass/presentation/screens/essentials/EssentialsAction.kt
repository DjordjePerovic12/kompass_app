package llc.bokadev.kompass.presentation.screens.essentials

import llc.bokadev.kompass.core.presentation.mvi.BaseAction
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.presentation.shared.title

sealed interface EssentialsAction : BaseAction {
    data class SelectTab(val tab: EssentialsTab) : EssentialsAction
    data class ToggleItem(val id: String) : EssentialsAction
    /** Expands a module (e.g. from "How it works") without collapsing it if it's already open. */
    data class ExpandItem(val id: String) : EssentialsAction
    data object Retry : EssentialsAction
    data object HideReminder : EssentialsAction
    data object OpenLanguage : EssentialsAction
    /** null [category] = "All on map"; [selectedId] pre-selects that pin. */
    data class OpenUtilityMap(val category: UtilityCategory?, val selectedId: String?) : EssentialsAction
    data class Dial(val number: String) : EssentialsAction
    data class OpenLocation(val title: String, val latitude: Double, val longitude: Double) : EssentialsAction
    data class OpenLink(val url: String) : EssentialsAction
    data object TurnOnLocation : EssentialsAction
    data object Back : EssentialsAction
}
