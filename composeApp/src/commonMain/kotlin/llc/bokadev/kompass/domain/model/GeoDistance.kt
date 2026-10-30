package llc.bokadev.kompass.domain.model

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Kotor Old Town's centre: the reference point for "how far out" rules that can't rely on GPS. */
val KotorOldTownCenter = GeoPoint(latitude = 42.4247, longitude = 18.7712)

/** Great-circle distance in km. */
fun GeoPoint.distanceKmTo(other: GeoPoint): Double {
    val dLat = (other.latitude - latitude).toRadians()
    val dLon = (other.longitude - longitude).toRadians()
    val a = sin(dLat / 2).pow(2) + cos(latitude.toRadians()) * cos(other.latitude.toRadians()) * sin(dLon / 2).pow(2)
    return 6371.0 * 2 * asin(sqrt(a))
}

private fun Double.toRadians(): Double = this * (PI / 180.0)
