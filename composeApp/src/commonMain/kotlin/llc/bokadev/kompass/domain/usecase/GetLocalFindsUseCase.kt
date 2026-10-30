package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.repository.LocalFindRepository

class GetLocalFindsUseCase(
    private val repository: LocalFindRepository
) {
    suspend operator fun invoke(): Result<List<LocalFind>> = repository.getLocalFinds()
}
