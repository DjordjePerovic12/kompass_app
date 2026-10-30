package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.domain.model.Excursion

@Serializable
data class ExcursionDto(
    val id: String,
    @SerialName("is_active") val isActive: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    val interests: List<String> = emptyList(),
    @SerialName("time_band") val timeBand: String? = null,
    @SerialName("planning_level") val planningLevel: String? = null,
    val place: PlaceDto? = null,
    val activity: ExperienceDto? = null
) {
    fun toDomainOrNull(): Excursion? {
        if (!isActive || !((place != null) xor (activity != null))) return null
        if (place?.isActive == false || activity?.isActive == false) return null
        return Excursion(id, place?.toDomain(), activity?.toDomain(), interests.toSet(), timeBand, planningLevel, sortOrder)
    }
}

@Serializable
data class PlaceActivityDto(
    val id: String,
    @SerialName("is_active") val isActive: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    val activity: ExperienceDto? = null
)
