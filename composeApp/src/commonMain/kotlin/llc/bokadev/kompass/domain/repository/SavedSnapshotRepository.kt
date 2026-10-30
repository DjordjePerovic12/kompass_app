package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.SavedSnapshot

interface SavedSnapshotRepository {
    fun get(key: FavoriteKey): SavedSnapshot?
    fun putAll(snapshots: Map<FavoriteKey, SavedSnapshot>)
}
