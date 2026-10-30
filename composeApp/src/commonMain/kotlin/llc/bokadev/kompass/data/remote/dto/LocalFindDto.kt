package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalFindDto(
    @SerialName("id") val id: String,
    @SerialName("city_id") val cityId: String,
    @SerialName("title") val title: Map<String, String>,
    @SerialName("body") val body: Map<String, String>,
    @SerialName("audio_file") val audioFile: Map<String, String>? = null,
    @SerialName("deep_body") val deepBody: Map<String, String>? = null,
    @SerialName("deep_audio_file") val deepAudioFile: Map<String, String>? = null,
    @SerialName("deep_photos") val deepPhotos: List<String> = emptyList(),
    @SerialName("deep_anchor_mode") val deepAnchorMode: String? = null,
    @SerialName("deep_latitude") val deepLatitude: Double? = null,
    @SerialName("deep_longitude") val deepLongitude: Double? = null,
    @SerialName("location") val location: Map<String, String>? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("photos") val photos: List<String> = emptyList(),
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
