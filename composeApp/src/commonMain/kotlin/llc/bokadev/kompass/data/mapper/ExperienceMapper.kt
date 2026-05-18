package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.ExperienceDto
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.Experience

fun ExperienceDto.toDomain(): Experience = Experience(
    id = id,
    cityId = cityId,
    name = name,
    description = description,
    longDescription = longDescription,
    location = location,
    howToGetThere = howToGetThere,
    audioFile = audioFile,
    deepText = deepText,
    deepAudioFile = deepAudioFile,
    deepPhotos = deepPhotos,
    deepAnchorMode = deepAnchorMode,
    deepLatitude = deepLatitude,
    deepLongitude = deepLongitude,
    audioAccessTier = audioAccessTier,
    detailAccessTier = detailAccessTier,
    durationMin = durationMin,
    externalWebsite = externalWebsite,
    category = category,
    bestTime = bestTime.toBestTime(),
    latitude = latitude,
    longitude = longitude,
    photos = photos,
    isActive = isActive,
    sortOrder = sortOrder
)

private fun String.toBestTime(): BestTime = when (uppercase()) {
    "MORNING" -> BestTime.MORNING
    "AFTERNOON" -> BestTime.AFTERNOON
    "EVENING" -> BestTime.EVENING
    else -> BestTime.ANYTIME
}
