package llc.bokadev.kompass.presentation.screens.excursions

import llc.bokadev.kompass.core.presentation.mvi.BaseAction
import llc.bokadev.kompass.domain.model.ExcursionFilter
import llc.bokadev.kompass.domain.model.FavoriteItemType

sealed interface ExcursionsAction : BaseAction {
    data object Reload : ExcursionsAction
    data class Filter(val value: ExcursionFilter) : ExcursionsAction
    /** [saved] is the state the visitor asked for (not a blind flip). */
    data class SetSaved(val type: FavoriteItemType, val id: String, val saved: Boolean) : ExcursionsAction
    data class Open(val type: FavoriteItemType, val id: String) : ExcursionsAction
    data object Back : ExcursionsAction
}
