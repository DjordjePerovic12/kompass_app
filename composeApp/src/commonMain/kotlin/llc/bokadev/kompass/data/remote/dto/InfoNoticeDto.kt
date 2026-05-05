package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InfoNoticeDto(
    @SerialName("id") val id: String,
    @SerialName("city_id") val cityId: String,
    @SerialName("title") val title: Map<String, String>,
    @SerialName("short_description") val shortDescription: Map<String, String>,
    @SerialName("long_description") val longDescription: Map<String, String>? = null,
    @SerialName("priority") val priority: String = "general",
    @SerialName("notice_type") val noticeType: String = "general",
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("location") val location: Map<String, String>? = null,
    @SerialName("external_url") val externalUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
