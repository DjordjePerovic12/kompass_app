package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.repository.PlaceRepository

class GetMustSeePlacesUseCase(
    private val repository: PlaceRepository
) {
    suspend operator fun invoke(): Result<List<Place>> = repository.getMustSeePlaces()
}
