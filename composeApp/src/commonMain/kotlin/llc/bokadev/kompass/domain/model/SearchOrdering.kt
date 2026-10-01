package llc.bokadev.kompass.domain.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun List<SearchResult>.favoriteAndNearestFirst(
    favoriteKeys: Set<FavoriteKey>,
    origin: GeoPoint?
): List<SearchResult> {
    return mapIndexed { index, result ->
        val isFavorited = result.favoriteKeyOrNull()?.let { it in favoriteKeys } ?: false
        val distanceKm = if (origin != null && result.latitude != null && result.longitude != null) {
            haversineDistanceKm(origin, GeoPoint(result.latitude, result.longitude))
        } else {
            null
        }
        RankedSearchResult(
            result = result,
            originalIndex = index,
            isFavorited = isFavorited,
            distanceKm = distanceKm
        )
    }.sortedWith(
        compareByDescending<RankedSearchResult> { it.isFavorited }
            .thenBy { if (it.distanceKm != null) 0 else 1 }
            .thenBy { it.distanceKm ?: Double.MAX_VALUE }
            .thenBy { it.originalIndex }
    ).map { it.result }
}

private data class RankedSearchResult(
    val result: SearchResult,
    val originalIndex: Int,
    val isFavorited: Boolean,
    val distanceKm: Double?
)

private fun haversineDistanceKm(from: GeoPoint, to: GeoPoint): Double {
    val earthRadiusKm = 6371.0
    val dLat = (to.latitude - from.latitude).toRadians()
    val dLon = (to.longitude - from.longitude).toRadians()
    val fromLat = from.latitude.toRadians()
    val toLat = to.latitude.toRadians()

    val a = sin(dLat / 2).pow(2) +
        cos(fromLat) * cos(toLat) * sin(dLon / 2).pow(2)
    val c = 2 * asin(sqrt(a))
    return earthRadiusKm * c
}

private fun Double.toRadians(): Double = this * (kotlin.math.PI / 180.0)
