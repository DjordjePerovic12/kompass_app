package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.CityEssentialDto
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory

fun CityEssentialDto.toDomain(): CityEssential = CityEssential(
    id = id,
    key = key,
    category = category.toEssentialCategory(),
    title = title,
    content = content,
    location = location,
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
