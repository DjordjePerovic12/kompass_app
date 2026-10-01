package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.repository.LocalFindRepository

class GetLocalFindByIdUseCase(
    private val repository: LocalFindRepository
) {
    suspend operator fun invoke(id: String): Result<LocalFind> = repository.getLocalFindById(id)
}
