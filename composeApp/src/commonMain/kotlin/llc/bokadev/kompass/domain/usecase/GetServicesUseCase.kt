package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.domain.repository.ServiceRepository

class GetServicesUseCase(
    private val repository: ServiceRepository
) {
    suspend operator fun invoke(): Resource<List<Service>, NetworkError, String?> = repository.getServices()
}
