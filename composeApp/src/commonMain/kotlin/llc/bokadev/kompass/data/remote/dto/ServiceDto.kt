package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: Map<String, String>,
    @SerialName("description") val description: Map<String, String>,
    @SerialName("location") val location: Map<String, String>? = null,
    @SerialName("external_website") val externalWebsite: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
)
