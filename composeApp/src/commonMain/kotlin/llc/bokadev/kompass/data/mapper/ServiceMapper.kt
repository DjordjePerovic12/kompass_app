package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.ServiceDto
import llc.bokadev.kompass.domain.model.Service

fun ServiceDto.toDomain(): Service = Service(
    id = id,
    name = name,
    description = description,
    location = location,
    externalWebsite = externalWebsite,
    latitude = latitude,
    longitude = longitude,
    sortOrder = sortOrder
)
