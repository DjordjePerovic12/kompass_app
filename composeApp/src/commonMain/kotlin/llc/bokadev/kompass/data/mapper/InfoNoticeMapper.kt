package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.InfoNoticeDto
import llc.bokadev.kompass.domain.model.InfoNotice

fun InfoNoticeDto.toDomain(): InfoNotice = InfoNotice(
    id = id,
    cityId = cityId,
    title = title,
    shortDescription = shortDescription,
    longDescription = longDescription,
    imageUrl = imageUrl,
    priority = priority,
    noticeType = noticeType,
    startsAt = startsAt,
    endsAt = endsAt,
    location = location,
    externalUrl = externalUrl,
    isActive = isActive,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)
