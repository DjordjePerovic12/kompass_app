package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.domain.repository.ServiceRepository

class GetServiceByIdUseCase(
    private val repository: ServiceRepository
) {
    suspend operator fun invoke(id: String): Result<Service> = repository.getServiceById(id)
}
