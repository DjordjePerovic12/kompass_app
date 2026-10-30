package llc.bokadev.kompass.presentation.categorylist

import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class CategoryListEvent : BaseEvent {
    data object NavigateBack : CategoryListEvent()
    data object OnRetryClick : CategoryListEvent()
    data object OnSearchClick : CategoryListEvent()
    data class OnItemClick(val item: ListItem) : CategoryListEvent()

    /** [saved] is the state the visitor asked for, so a repeated tap event can't flip it back. */
    data class OnSavedChange(val item: ListItem, val saved: Boolean) : CategoryListEvent()
    data class OnAreaChange(val areaKey: String?) : CategoryListEvent()
    data class OnUndoClick(val entry: FavoriteEntry) : CategoryListEvent()

    /** Emitted by the ViewModel on eventFlow: the save confirmation snackbar. */
    data class ShowSaved(val item: ListItem) : CategoryListEvent()

    /** Emitted by the ViewModel on eventFlow: the "Removed · Undo" snackbar. */
    data class ShowRemoved(val item: ListItem, val entry: FavoriteEntry) : CategoryListEvent()
}
