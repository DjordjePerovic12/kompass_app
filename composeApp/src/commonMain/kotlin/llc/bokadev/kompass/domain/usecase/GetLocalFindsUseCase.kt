package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.repository.LocalFindRepository

class GetLocalFindsUseCase(
    private val repository: LocalFindRepository
) {
    suspend operator fun invoke(): Resource<List<LocalFind>, NetworkError, String?> = repository.getLocalFinds()
}
