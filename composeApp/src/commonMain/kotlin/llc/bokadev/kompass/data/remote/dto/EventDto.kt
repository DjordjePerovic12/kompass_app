package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: Map<String, String>,
    @SerialName("description") val description: Map<String, String>,
    @SerialName("venue") val venue: Map<String, String>,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("category") val category: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("price") val price: String? = null,
    @SerialName("ticket_url") val ticketUrl: String? = null,
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    @SerialName("photos") val photos: List<String> = emptyList(),
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)
