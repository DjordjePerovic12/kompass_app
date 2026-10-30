package llc.bokadev.kompass.data.repository

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.SavedSnapshot
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository

/** Stored next to the favorites (same preferences), keyed "PLACE:id" / "ACTIVITY:id". */
class SavedSnapshotRepositoryImpl(private val preferences: AppPreferences) : SavedSnapshotRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = MapSerializer(String.serializer(), SavedSnapshot.serializer())
    private var cache: MutableMap<String, SavedSnapshot> = load()

    override fun get(key: FavoriteKey): SavedSnapshot? = cache[key.storageKey()]

    override fun putAll(snapshots: Map<FavoriteKey, SavedSnapshot>) {
        if (snapshots.isEmpty()) return
        snapshots.forEach { (key, snapshot) -> cache[key.storageKey()] = snapshot }
        preferences.setString(KEY, json.encodeToString(serializer, cache))
    }

    private fun load(): MutableMap<String, SavedSnapshot> =
        preferences.getString(KEY)
            ?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() }
            ?.toMutableMap()
            ?: mutableMapOf()

    private fun FavoriteKey.storageKey() = "${type.name}:$id"

    private companion object {
        const val KEY = "saved_snapshots_v1"
    }
}
