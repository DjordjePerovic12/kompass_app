package llc.bokadev.kompass.domain.repository

import kotlinx.coroutines.flow.StateFlow
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey

interface FavoritesRepository {
    val favoritesFlow: StateFlow<List<FavoriteEntry>>

    fun isFavorited(type: FavoriteItemType, id: String): Boolean

    /** Saves the item unless it's already saved (idempotent); returns the stored entry. Persists before returning. */
    fun save(type: FavoriteItemType, id: String): FavoriteEntry

    /** Removes the item if saved (idempotent); returns the removed entry for Undo, or null if it wasn't saved. */
    fun remove(type: FavoriteItemType, id: String): FavoriteEntry?

    /**
     * Flips the current state. Prefer [save]/[remove] with the state the visitor saw, so a repeated
     * event can't undo itself.
     */
    fun toggleFavorite(type: FavoriteItemType, id: String) {
        if (remove(type, id) == null) save(type, id)
    }

    /** Puts a removed entry back with its original timestamp (Undo), so it returns to the same spot. */
    fun restore(entry: FavoriteEntry)
    fun getFavoriteKeySet(): Set<FavoriteKey>
}
