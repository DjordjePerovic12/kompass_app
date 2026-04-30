package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    @SerialName("id")         val id: String,
    @SerialName("name")       val name: Map<String, String>,
    @SerialName("icon")       val icon: String? = null,
    @SerialName("color")      val color: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active")  val isActive: Boolean = true
)
