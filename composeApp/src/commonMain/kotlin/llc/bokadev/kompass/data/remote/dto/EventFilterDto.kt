package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventFilterDto(
    @SerialName("id") val id: String,
    @SerialName("key") val key: String,
    @SerialName("kind") val kind: String,
    @SerialName("label") val label: Map<String, String>,
    @SerialName("value") val value: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
)
