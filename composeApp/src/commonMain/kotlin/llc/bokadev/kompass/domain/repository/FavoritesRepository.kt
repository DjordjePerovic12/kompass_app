package llc.bokadev.kompass.domain.repository

import kotlinx.coroutines.flow.StateFlow
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey

interface FavoritesRepository {
    val favoritesFlow: StateFlow<List<FavoriteEntry>>

    fun isFavorited(type: FavoriteItemType, id: String): Boolean
    fun toggleFavorite(type: FavoriteItemType, id: String)
    fun getFavoriteKeySet(): Set<FavoriteKey>
}
