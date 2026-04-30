package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.EventDto
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.model.EventFilterKind
import llc.bokadev.kompass.data.remote.dto.EventFilterDto

fun EventDto.toDomain(): Event = Event(
    id = id,
    name = name,
    description = description,
    venue = venue,
    latitude = latitude,
    longitude = longitude,
    category = category,
    startTime = startTime,
    endTime = endTime,
    price = price,
    ticketUrl = ticketUrl,
    isRecurring = isRecurring,
    photos = photos
)

fun EventFilterDto.toDomain(): EventFilter = EventFilter(
    id = id,
    key = key,
    kind = kind.toEventFilterKind(),
    label = label,
    value = value,
    sortOrder = sortOrder,
    isActive = isActive
)

private fun String.toEventFilterKind(): EventFilterKind = when (lowercase()) {
    "date" -> EventFilterKind.DATE
    else -> EventFilterKind.TYPE
}
