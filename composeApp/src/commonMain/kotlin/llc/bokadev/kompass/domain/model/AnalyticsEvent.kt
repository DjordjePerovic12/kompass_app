package llc.bokadev.kompass.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsEvent(
    @SerialName("event_name") val eventName: String,
    @SerialName("city_id") val cityId: String? = null,
    @SerialName("city_slug") val citySlug: String? = "kotor",
    @SerialName("app_language") val appLanguage: String,
    @SerialName("app_version") val appVersion: String,
    val platform: String,
    @SerialName("anonymous_user_id") val anonymousUserId: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("category_key") val categoryKey: String? = null,
    @SerialName("place_id") val placeId: String? = null,
    @SerialName("activity_id") val activityId: String? = null,
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("info_notice_id") val infoNoticeId: String? = null,
    val zone: String? = null,
    @SerialName("place_category") val placeCategory: String? = null,
    @SerialName("content_origin") val contentOrigin: String? = null,
    val metadata: Map<String, String> = emptyMap()
)
