package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.repository.PlaceRepository

class GetPlaceByIdUseCase(private val repository: PlaceRepository) {
    suspend operator fun invoke(id: String): Resource<Place, NetworkError, String?> =
        repository.getPlaceById(id)
}
