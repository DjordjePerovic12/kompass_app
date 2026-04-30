package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.repository.EssentialRepository

class GetEssentialsUseCase(
    private val repository: EssentialRepository
) {
    suspend operator fun invoke(): Result<List<CityEssential>> = repository.getEssentials()
}
