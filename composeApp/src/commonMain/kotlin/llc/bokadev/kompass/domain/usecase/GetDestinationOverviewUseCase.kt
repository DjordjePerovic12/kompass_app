package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.mapCatching
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.distanceKmTo
import llc.bokadev.kompass.domain.repository.PlaceRepository

/**
 * The editorial introduction for where the visitor is browsing: the overview whose zone matches a
 * chosen area, or (with a real location) the nearest overview within [maxDistanceKm]. Null when
 * the CMS has none — never a guessed substitute.
 */
class GetDestinationOverviewUseCase(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(
        matchesZone: ((zone: String?) -> Boolean)?,
        origin: GeoPoint?,
        maxDistanceKm: Double
    ): Resource<Place?, NetworkError, String?> = repository.getActivePlaces().mapCatching { places ->
        val overviews = places.filter { it.isDestinationOverview }
        when {
            matchesZone != null -> overviews.firstOrNull { matchesZone(it.zone) }
            origin != null -> overviews
                .map { it to origin.distanceKmTo(GeoPoint(it.latitude, it.longitude)) }
                .filter { it.second <= maxDistanceKm }
                .minByOrNull { it.second }?.first
            else -> null
        }
    }
}
