package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.repository.PlaceRepository

class GetPlaceByIdUseCase(private val repository: PlaceRepository) {
    suspend operator fun invoke(id: String): Result<Place> =
        repository.getPlaceById(id)
}
