package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.CityEssentialDto
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialLocationPoint
import llc.bokadev.kompass.domain.model.EssentialCategory

fun CityEssentialDto.toDomain(): CityEssential = CityEssential(
    id = id,
    key = key,
    category = category.toEssentialCategory(),
    title = title,
    content = content,
    location = location,
    latitude = latitude,
    longitude = longitude,
    locations = locations?.map {
        EssentialLocationPoint(
            title = it.title,
            latitude = it.latitude,
            longitude = it.longitude
        )
    } ?: emptyList(),
    links = links ?: emptyList(),
    sortOrder = sortOrder
)

private fun String.toEssentialCategory(): EssentialCategory = when (lowercase()) {
    "transport"  -> EssentialCategory.TRANSPORT
    "customs"    -> EssentialCategory.CUSTOMS
    "emergency"  -> EssentialCategory.EMERGENCY
    "tips"       -> EssentialCategory.TIPS
    else         -> EssentialCategory.PRACTICAL
}
