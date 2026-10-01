package llc.bokadev.kompass.presentation.screens.essentials

import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Utility
import llc.bokadev.kompass.domain.model.UtilityCategory
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class UtilityCategorySection(
    val category: UtilityCategory,
    val totalCount: Int,
    val nearbyCount: Int,
    val closestDistanceMeters: Int?,
    val previewUtilities: List<Utility>,
    val mapCenter: GeoPoint,
    val showCurrentLocation: Boolean
)

data class UtilityLocationItem(
    val utility: Utility,
    val point: GeoPoint,
    val distanceMeters: Int?
)

internal fun buildUtilitySection(
    category: UtilityCategory,
    items: List<Utility>,
    origin: GeoPoint,
    currentLocation: GeoPoint?
): UtilityCategorySection? {
    val ranked = items
        .mapNotNull { utility ->
            val lat = utility.latitude ?: return@mapNotNull null
            val lng = utility.longitude ?: return@mapNotNull null
            RankedUtility(
                utility = utility,
                point = GeoPoint(lat, lng),
                distanceKm = haversineDistanceKm(origin, GeoPoint(lat, lng))
            )
        }
        .sortedBy { it.distanceKm }

    if (ranked.isEmpty()) return null

    val nearby = ranked.filter { it.distanceKm <= NEARBY_RADIUS_KM }
    val preview = if (nearby.isNotEmpty()) {
        nearby.take(MAX_PREVIEW_PINS)
    } else {
        val anchor = ranked.first()
        ranked
            .filter { candidate ->
                haversineDistanceKm(anchor.point, candidate.point) <= FALLBACK_CLUSTER_RADIUS_KM
            }
            .take(FALLBACK_PREVIEW_PINS)
            .ifEmpty { listOf(anchor) }
    }
    val previewPoints = preview.map { it.point }
    val center = GeoPoint(
        latitude = previewPoints.map { it.latitude }.average(),
        longitude = previewPoints.map { it.longitude }.average()
    )
    val showCurrentLocation = currentLocation?.let {
        preview.minOfOrNull { rankedUtility -> rankedUtility.distanceKm }?.let { closest ->
            closest <= CURRENT_LOCATION_PREVIEW_LIMIT_KM
        } ?: false
    } ?: false

    return UtilityCategorySection(
        category = category,
        totalCount = ranked.size,
        nearbyCount = nearby.size,
        closestDistanceMeters = (ranked.firstOrNull()?.distanceKm?.times(1000))?.toInt(),
        previewUtilities = preview.map { it.utility },
        mapCenter = center,
        showCurrentLocation = showCurrentLocation
    )
}

internal fun buildUtilityLocationItems(
    category: UtilityCategory,
    items: List<Utility>,
    origin: GeoPoint
): List<UtilityLocationItem> {
    return items
        .filter { it.category == category }
        .mapNotNull { utility ->
            val lat = utility.latitude ?: return@mapNotNull null
            val lng = utility.longitude ?: return@mapNotNull null
            val point = GeoPoint(lat, lng)
            UtilityLocationItem(
                utility = utility,
                point = point,
                distanceMeters = (haversineDistanceKm(origin, point) * 1000).toInt()
            )
        }
        .sortedBy { it.distanceMeters ?: Int.MAX_VALUE }
        .let { ranked ->
            val nearby = ranked.filter { (it.distanceMeters ?: Int.MAX_VALUE) <= (NEARBY_RADIUS_KM * 1000).toInt() }
            if (nearby.isNotEmpty()) {
                nearby.take(MAX_DETAIL_PINS)
            } else {
                val anchor = ranked.firstOrNull() ?: return emptyList()
                ranked
                    .filter { candidate ->
                        haversineDistanceKm(anchor.point, candidate.point) <= FALLBACK_CLUSTER_RADIUS_KM
                    }
                    .take(FALLBACK_PREVIEW_PINS)
                    .ifEmpty { listOf(anchor) }
            }
        }
}

internal fun UtilityCategory.orderIndex(): Int = when (this) {
    UtilityCategory.ATM -> 0
    UtilityCategory.PHARMACY -> 1
    UtilityCategory.SUPERMARKET -> 2
    UtilityCategory.SHOP -> 3
    UtilityCategory.PARKING -> 4
    UtilityCategory.GAS_STATION -> 5
    UtilityCategory.EMERGENCY -> 6
}

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

private data class RankedUtility(
    val utility: Utility,
    val point: GeoPoint,
    val distanceKm: Double
)

internal const val NEARBY_RADIUS_KM = 1.2
private const val MAX_PREVIEW_PINS = 5
private const val MAX_DETAIL_PINS = 6
private const val CURRENT_LOCATION_PREVIEW_LIMIT_KM = 2.8
private const val FALLBACK_CLUSTER_RADIUS_KM = 1.6
private const val FALLBACK_PREVIEW_PINS = 3

internal val KOTOR_OLD_TOWN_CENTER = GeoPoint(
    latitude = 42.4246,
    longitude = 18.7712
)
