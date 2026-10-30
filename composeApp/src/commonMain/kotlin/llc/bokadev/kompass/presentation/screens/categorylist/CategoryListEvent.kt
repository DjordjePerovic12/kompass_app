package llc.bokadev.kompass.presentation.screens.categorylist

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent
import llc.bokadev.kompass.domain.model.FavoriteEntry

/** One-off results of saving/removing, for the confirmation and Undo snackbars. */
sealed interface CategoryListEvent : BaseEvent {
    data class Saved(val item: ListItem) : CategoryListEvent
    data class Removed(val item: ListItem, val entry: FavoriteEntry) : CategoryListEvent
}
