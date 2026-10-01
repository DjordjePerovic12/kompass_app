package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.repository.UtilityRepository

class GetUtilitiesUseCase(
    private val repository: UtilityRepository
) {
    suspend operator fun invoke() = repository.getUtilities()
}
