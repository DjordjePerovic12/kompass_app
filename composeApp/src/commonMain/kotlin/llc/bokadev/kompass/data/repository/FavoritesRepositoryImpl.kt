package llc.bokadev.kompass.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.model.FavoriteEntry
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Clock

@OptIn(ExperimentalTime::class)
class FavoritesRepositoryImpl(
    private val appPreferences: AppPreferences
) : FavoritesRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _favoritesFlow = MutableStateFlow(loadFavorites())

    override val favoritesFlow: StateFlow<List<FavoriteEntry>> = _favoritesFlow.asStateFlow()

    override fun isFavorited(type: FavoriteItemType, id: String): Boolean {
        return _favoritesFlow.value.any { it.type == type && it.id == id }
    }

    override fun toggleFavorite(type: FavoriteItemType, id: String) {
        val updated = _favoritesFlow.value.toMutableList().apply {
            val existingIndex = indexOfFirst { it.type == type && it.id == id }
            if (existingIndex >= 0) {
                removeAt(existingIndex)
            } else {
                add(
                    FavoriteEntry(
                        type = type,
                        id = id,
                        favoritedAtEpochMs = Clock.System.now().toEpochMilliseconds()
                    )
                )
            }
        }
        _favoritesFlow.value = updated
        persistFavorites(updated)
    }

    override fun getFavoriteKeySet(): Set<FavoriteKey> {
        return _favoritesFlow.value.map { FavoriteKey(it.type, it.id) }.toSet()
    }

    private fun loadFavorites(): List<FavoriteEntry> {
        val raw = appPreferences.getString(FAVORITES_KEY) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(FavoriteEntry.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    private fun persistFavorites(favorites: List<FavoriteEntry>) {
        appPreferences.setString(
            FAVORITES_KEY,
            json.encodeToString(ListSerializer(FavoriteEntry.serializer()), favorites)
        )
    }

    private companion object {
        const val FAVORITES_KEY = "favorites_v1"
    }
}
