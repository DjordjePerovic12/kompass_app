package llc.bokadev.kompass.data.repository

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.domain.repository.RecentSearchesRepository

/** Up to 5 recent queries, most recent first, stored only on the device. */
class RecentSearchesStore(private val preferences: AppPreferences) : RecentSearchesRepository {
    private val serializer = ListSerializer(String.serializer())

    override fun get(): List<String> =
        preferences.getString(KEY)?.let { runCatching { Json.decodeFromString(serializer, it) }.getOrNull() }.orEmpty()

    override fun add(query: String) {
        val q = query.trim().takeIf { it.isNotBlank() } ?: return
        val updated = (listOf(q) + get().filterNot { it.equals(q, ignoreCase = true) }).take(MAX)
        preferences.setString(KEY, Json.encodeToString(serializer, updated))
    }

    override fun clear() = preferences.setString(KEY, null)

    private companion object {
        const val KEY = "recent_searches_v1"
        const val MAX = 5
    }
}
