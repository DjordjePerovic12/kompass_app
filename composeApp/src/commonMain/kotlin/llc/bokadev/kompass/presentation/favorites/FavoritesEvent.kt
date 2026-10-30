package llc.bokadev.kompass.presentation.favorites

import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.presentation.base.BaseEvent

sealed class FavoritesEvent : BaseEvent {
    data object OnRetryClick : FavoritesEvent()
    data object OnLanguageClick : FavoritesEvent()
    data object OnExploreKotorClick : FavoritesEvent()
    data class OnItemClick(val type: FavoriteItemType, val id: String) : FavoritesEvent()

    /** [title] is what the visitor saw, for the "Removed {title}" confirmation. */
    data class OnRemoveClick(val entry: FavoriteEntry, val title: String) : FavoritesEvent()
    data class OnUndoClick(val entry: FavoriteEntry) : FavoritesEvent()

    /** Emitted by the ViewModel on eventFlow: the "Removed {title} · Undo" snackbar. */
    data class ShowRemoved(val entry: FavoriteEntry, val title: String) : FavoritesEvent()
}
