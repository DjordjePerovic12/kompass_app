package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.repository.EssentialRepository

class GetEssentialsUseCase(
    private val repository: EssentialRepository
) {
    suspend operator fun invoke(): Resource<List<CityEssential>, NetworkError, String?> = repository.getEssentials()
}
