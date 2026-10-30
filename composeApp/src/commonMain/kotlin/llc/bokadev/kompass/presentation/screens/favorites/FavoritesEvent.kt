package llc.bokadev.kompass.presentation.screens.favorites

import llc.bokadev.kompass.core.presentation.mvi.BaseEvent
import llc.bokadev.kompass.domain.model.FavoriteEntry

sealed interface FavoritesEvent : BaseEvent {
    /** Show "Removed {title}" with Undo. */
    data class Removed(val entry: FavoriteEntry, val title: String) : FavoritesEvent
}
