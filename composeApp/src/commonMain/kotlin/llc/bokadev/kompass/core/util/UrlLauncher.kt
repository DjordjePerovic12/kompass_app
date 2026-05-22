package llc.bokadev.kompass.core.util

expect fun buildMapsUrl(query: String): String
expect fun buildMapsUrlForCoords(lat: Double, lng: Double): String
expect fun buildMapsDirectionsUrlForCoords(lat: Double, lng: Double): String
expect fun buildNearbyUtilityMapsUrl(
    categoryQuery: String,
    centerLat: Double,
    centerLng: Double,
    currentLat: Double? = null,
    currentLng: Double? = null,
    fallbackLat: Double? = null,
    fallbackLng: Double? = null
): String
