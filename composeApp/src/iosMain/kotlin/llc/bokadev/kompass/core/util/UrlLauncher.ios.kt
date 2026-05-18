package llc.bokadev.kompass.core.util

actual fun buildMapsUrl(query: String): String {
    val encoded = query
        .replace(" ", "+")
        .replace("&", "%26")
        .replace("#", "%23")
    return "maps://?q=$encoded"
}

actual fun buildMapsUrlForCoords(lat: Double, lng: Double): String =
    "maps://?ll=$lat,$lng"

actual fun buildNearbyUtilityMapsUrl(
    categoryQuery: String,
    centerLat: Double,
    centerLng: Double,
    currentLat: Double?,
    currentLng: Double?,
    fallbackLat: Double?,
    fallbackLng: Double?
): String {
    val encoded = categoryQuery.appleMapsUtilityQuery()
        .replace(" ", "+")
        .replace("&", "%26")
        .replace("#", "%23")
    return if (currentLat != null && currentLng != null) {
        "maps://?q=$encoded&sll=$currentLat,$currentLng&z=15"
    } else if (fallbackLat != null && fallbackLng != null) {
        "maps://?q=$encoded&ll=$fallbackLat,$fallbackLng&z=15"
    } else {
        "maps://?q=$encoded&ll=$centerLat,$centerLng&z=15"
    }
}

private fun String.appleMapsUtilityQuery(): String = when (trim().lowercase()) {
    "atm" -> "ATM"
    "pharmacy" -> "Pharmacy"
    "supermarket" -> "Grocery Store"
    "shop" -> "Convenience Store"
    "parking" -> "Parking"
    "gas station" -> "Gas Station"
    else -> this
}
