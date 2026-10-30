package llc.bokadev.kompass.presentation.screens.categorylist

import llc.bokadev.kompass.core.presentation.mvi.BaseAction
import llc.bokadev.kompass.domain.model.FavoriteEntry

sealed interface CategoryListAction : BaseAction {
    /** [saved] is the state the visitor asked for, so a repeated tap event can't flip it back. */
    data class SetSaved(val item: ListItem, val saved: Boolean) : CategoryListAction
    data class Undo(val entry: FavoriteEntry) : CategoryListAction
    data class SetArea(val areaKey: String?) : CategoryListAction
    data class Open(val item: ListItem) : CategoryListAction
    data object OpenSearch : CategoryListAction
    data object Retry : CategoryListAction
    data object Back : CategoryListAction
}
