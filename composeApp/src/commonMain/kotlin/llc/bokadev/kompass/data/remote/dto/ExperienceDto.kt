package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExperienceDto(
    @SerialName("id") val id: String,
    @SerialName("city_id") val cityId: String,
    @SerialName("name") val name: Map<String, String>,
    @SerialName("description") val description: Map<String, String>,
    @SerialName("long_description") val longDescription: Map<String, String>? = null,
    @SerialName("location") val location: Map<String, String>? = null,
    @SerialName("how_to_get_there") val howToGetThere: Map<String, String>? = null,
    @SerialName("audio_file") val audioFile: String? = null,
    @SerialName("audio_access_tier") val audioAccessTier: String = "audio_pass",
    @SerialName("detail_access_tier") val detailAccessTier: String = "explorer_pass",
    @SerialName("duration_min") val durationMin: Int? = null,
    @SerialName("external_website") val externalWebsite: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("best_time") val bestTime: String = "any",
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("photos") val photos: List<String> = emptyList(),
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)
