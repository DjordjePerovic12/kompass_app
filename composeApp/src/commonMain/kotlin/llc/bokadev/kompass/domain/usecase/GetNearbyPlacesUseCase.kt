package llc.bokadev.kompass.domain.usecase

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.repository.PlaceRepository

class GetNearbyPlacesUseCase(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(
        origin: GeoPoint,
        limit: Int? = null
    ): Result<List<NearbyPlace>> = repository.getActivePlaces().mapCatching { places ->
        val sorted = places
            .asSequence()
            .map { place ->
                NearbyPlace(
                    place = place,
                    distanceKm = haversineDistanceKm(origin, GeoPoint(place.latitude, place.longitude))
                )
            }
            .sortedBy { it.distanceKm }
            .toList()

        if (limit == null) sorted else sorted.take(limit)
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
}
