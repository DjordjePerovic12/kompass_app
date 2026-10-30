package llc.bokadev.kompass.testutil

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.repository.FavoritesRepository

/** In-memory [FavoritesRepository] with the real store's rules (idempotent save/remove, restore keeps the entry). */
class FakeFavorites(initial: List<FavoriteEntry>) : FavoritesRepository {
    private val flow = MutableStateFlow(initial)
    override val favoritesFlow: StateFlow<List<FavoriteEntry>> = flow
    override fun isFavorited(type: FavoriteItemType, id: String) = flow.value.any { it.type == type && it.id == id }
    override fun save(type: FavoriteItemType, id: String): FavoriteEntry =
        flow.value.firstOrNull { it.type == type && it.id == id } ?: FavoriteEntry(type, id, 99).also { flow.value = flow.value + it }
    override fun remove(type: FavoriteItemType, id: String): FavoriteEntry? =
        flow.value.firstOrNull { it.type == type && it.id == id }?.also { e -> flow.value = flow.value - e }
    override fun restore(entry: FavoriteEntry) { if (!isFavorited(entry.type, entry.id)) flow.value = flow.value + entry }
    override fun getFavoriteKeySet() = flow.value.map { FavoriteKey(it.type, it.id) }.toSet()
}
