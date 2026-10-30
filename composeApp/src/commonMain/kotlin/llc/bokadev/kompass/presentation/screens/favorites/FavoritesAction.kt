package llc.bokadev.kompass.presentation.screens.favorites

import llc.bokadev.kompass.core.presentation.mvi.BaseAction
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType

sealed interface FavoritesAction : BaseAction {
    data object Load : FavoritesAction
    /** [title] is what the visitor saw, for the "Removed {title}" confirmation. */
    data class Remove(val entry: FavoriteEntry, val title: String) : FavoritesAction
    data class Undo(val entry: FavoriteEntry) : FavoritesAction
    data class Open(val type: FavoriteItemType, val id: String) : FavoritesAction
    data object ExploreKotor : FavoritesAction
    data object OpenLanguage : FavoritesAction
}
