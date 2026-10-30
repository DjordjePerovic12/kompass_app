package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.domain.repository.ServiceRepository

class GetServiceByIdUseCase(
    private val repository: ServiceRepository
) {
    suspend operator fun invoke(id: String): Resource<Service, NetworkError, String?> = repository.getServiceById(id)
}
