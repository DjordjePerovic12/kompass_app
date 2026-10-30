package llc.bokadev.kompass.domain.model

import kotlinx.serialization.Serializable

/**
 * What Saved remembers about an item the last time it resolved, so a saved place that is later
 * unpublished (or seen offline) still shows its name instead of a blank card.
 */
@Serializable
data class SavedSnapshot(
    val name: Map<String, String> = emptyMap(),
    val groupKey: String? = null,
    val photo: String? = null
)

/** What to remember about a place at the moment it is saved (or later resolved). */
fun Place.toSavedSnapshot(groupKey: String?) = SavedSnapshot(name = name, groupKey = groupKey, photo = photos.firstOrNull())

fun Experience.toSavedSnapshot(groupKey: String?) = SavedSnapshot(name = name, groupKey = groupKey, photo = photos.firstOrNull())
