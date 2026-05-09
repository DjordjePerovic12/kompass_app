package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    @SerialName("id") val id: String,
    @SerialName("city_id") val cityId: String,
    @SerialName("name") val name: Map<String, String>,
    @SerialName("description") val description: Map<String, String>,
    @SerialName("locals_tip") val localsTip: Map<String, String>,
    @SerialName("category") val category: String,
    @SerialName("sub_category") val subCategory: String? = null,
    @SerialName("price_indicator") val priceIndicator: String? = null,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("zone") val zone: String? = null,
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("best_time") val bestTime: String,
    @SerialName("estimated_duration") val estimatedDuration: Int? = null,
    @SerialName("opening_hours") val openingHours: Map<String, String>? = null,
    @SerialName("photos") val photos: List<String> = emptyList(),
    @SerialName("audio_file") val audioFile: String? = null,
    @SerialName("audio_access_tier") val audioAccessTier: String = "audio_pass",
    @SerialName("detail_access_tier") val detailAccessTier: String = "explorer_pass",
    @SerialName("is_must_see") val isMustSee: Boolean = false,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("sort_order") val sortOrder: Int,
    @SerialName("long_description") val longDescription: Map<String, String>? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
