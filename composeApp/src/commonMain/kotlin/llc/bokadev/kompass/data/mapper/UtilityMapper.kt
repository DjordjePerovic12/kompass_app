package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.UtilityDto
import llc.bokadev.kompass.domain.model.Utility
import llc.bokadev.kompass.domain.model.UtilityCategory

fun UtilityDto.toDomain(): Utility = Utility(
    id = id,
    name = name,
    category = category.toUtilityCategory(),
    latitude = latitude,
    longitude = longitude,
    address = address,
    googlePlaceId = googlePlaceId,
    sortOrder = sortOrder
)

private fun String.toUtilityCategory(): UtilityCategory = when (lowercase()) {
    "atm" -> UtilityCategory.ATM
    "pharmacy" -> UtilityCategory.PHARMACY
    "supermarket" -> UtilityCategory.SUPERMARKET
    "shop" -> UtilityCategory.SHOP
    "parking" -> UtilityCategory.PARKING
    "gas_station" -> UtilityCategory.GAS_STATION
    "emergency" -> UtilityCategory.EMERGENCY
    else -> UtilityCategory.SHOP
}
