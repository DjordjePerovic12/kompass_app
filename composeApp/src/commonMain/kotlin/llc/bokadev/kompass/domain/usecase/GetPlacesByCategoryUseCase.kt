package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.repository.PlaceRepository

class GetPlacesByCategoryUseCase(private val repository: PlaceRepository) {
    suspend operator fun invoke(category: PlaceCategory): Result<List<Place>> =
        repository.getPlacesByCategory(category)
}
