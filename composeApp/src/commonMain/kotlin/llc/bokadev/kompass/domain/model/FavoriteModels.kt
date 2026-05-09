package llc.bokadev.kompass.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class FavoriteItemType {
    PLACE,
    ACTIVITY
}

@Serializable
data class FavoriteEntry(
    val type: FavoriteItemType,
    val id: String,
    val favoritedAtEpochMs: Long
)

data class FavoriteKey(
    val type: FavoriteItemType,
    val id: String
)
