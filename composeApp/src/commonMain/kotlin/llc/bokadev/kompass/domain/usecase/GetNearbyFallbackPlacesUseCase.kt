package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.repository.PlaceRepository

/**
 * Nearby suggestions when there is no device location to sort by distance (permission denied,
 * timed out, or a manually-chosen area) — zone-matched instead of distance-sorted. Replaces a
 * direct `PlaceRepository.getActivePlaces()` call that previously sat inline in
 * `HomeViewModel`, bypassing the use-case layer the rest of the app's data access goes through.
 */
class GetNearbyFallbackPlacesUseCase(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(
        category: PlaceCategory,
        matchesZone: (zone: String?) -> Boolean,
        limit: Int
    ): Result<List<NearbyPlace>> = repository.getActivePlaces().mapCatching { places ->
        places
            .asSequence()
            .filter { it.category == category && matchesZone(it.zone) && !it.isDestinationOverview }
            .take(limit)
            // No synthetic origin: manual suggestions never display these unused distances.
            .map { NearbyPlace(it, 0.0) }
            .toList()
    }
}
