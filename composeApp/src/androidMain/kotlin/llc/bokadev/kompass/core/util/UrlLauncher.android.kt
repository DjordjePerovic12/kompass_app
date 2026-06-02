package llc.bokadev.kompass.core.util

import java.net.URLEncoder

actual fun buildMapsUrl(query: String): String {
    val encoded = URLEncoder.encode(query, "UTF-8")
    return "geo:0,0?q=$encoded"
}

actual fun buildMapsUrlForCoords(lat: Double, lng: Double): String =
    "https://www.google.com/maps/search/?api=1&query=$lat,$lng"

actual fun buildMapsDirectionsUrlForCoords(lat: Double, lng: Double): String =
    "google.navigation:q=$lat,$lng"

actual fun buildNearbyUtilityMapsUrl(
    categoryQuery: String,
    centerLat: Double,
    centerLng: Double,
    currentLat: Double?,
    currentLng: Double?,
    fallbackLat: Double?,
    fallbackLng: Double?
): String {
    val encoded = URLEncoder.encode("$categoryQuery near Kotor Montenegro", "UTF-8")
    return "geo:0,0?q=$encoded"
}
