package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.PlaceDto
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.model.PriceIndicator

fun PlaceDto.toDomain(): Place = Place(
    id = id,
    cityId = cityId,
    name = name,
    description = description,
    longDescription = longDescription ?: emptyMap(),
    localsTip = localsTip,
    category = category.toPlaceCategory(),
    subCategory = subCategory,
    priceIndicator = priceIndicator?.toPriceIndicator(),
    latitude = latitude,
    longitude = longitude,
    zone = zone,
    tags = tags,
    bestTime = bestTime.toBestTime(),
    estimatedDuration = estimatedDuration,
    openingHours = openingHours,
    photos = photos,
    audioFile = audioFile,
    audioAccessTier = audioAccessTier,
    detailAccessTier = detailAccessTier,
    isMustSee = isMustSee,
    isActive = isActive,
    sortOrder = sortOrder
)

private fun String.toPlaceCategory(): PlaceCategory = when (uppercase()) {
    "EAT_AND_DRINK" -> PlaceCategory.EAT_AND_DRINK
    "SEE_AND_VISIT" -> PlaceCategory.SEE_AND_VISIT
    "ACTIVITIES" -> PlaceCategory.ACTIVITIES
    "HIDDEN_GEMS" -> PlaceCategory.HIDDEN_GEMS
    "PRACTICAL" -> PlaceCategory.PRACTICAL
    else -> PlaceCategory.SEE_AND_VISIT
}

private fun String.toPriceIndicator(): PriceIndicator = when (toIntOrNull()) {
    1 -> PriceIndicator.BUDGET
    2 -> PriceIndicator.MODERATE
    3 -> PriceIndicator.EXPENSIVE
    else -> PriceIndicator.MODERATE
}

private fun String.toBestTime(): BestTime = when (uppercase()) {
    "MORNING" -> BestTime.MORNING
    "AFTERNOON" -> BestTime.AFTERNOON
    "EVENING" -> BestTime.EVENING
    else -> BestTime.ANYTIME
}
