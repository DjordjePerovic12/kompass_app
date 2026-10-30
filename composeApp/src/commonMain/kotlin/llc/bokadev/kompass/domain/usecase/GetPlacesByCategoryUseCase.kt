package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.repository.PlaceRepository

class GetPlacesByCategoryUseCase(private val repository: PlaceRepository) {
    suspend operator fun invoke(category: PlaceCategory): Resource<List<Place>, NetworkError, String?> =
        repository.getPlacesByCategory(category)
}
