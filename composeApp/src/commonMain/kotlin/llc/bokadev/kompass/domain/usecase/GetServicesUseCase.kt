package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.domain.repository.ServiceRepository

class GetServicesUseCase(
    private val repository: ServiceRepository
) {
    suspend operator fun invoke(): Result<List<Service>> = repository.getServices()
}
