package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.CategoryDto
import llc.bokadev.kompass.domain.model.Category

fun CategoryDto.toDomain() = Category(
    id        = id,
    name      = name,
    icon      = icon,
    color     = color,
    sortOrder = sortOrder,
    isActive  = isActive
)
