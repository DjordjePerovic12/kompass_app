package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.Service

interface ServiceRepository {
    suspend fun getServices(): Result<List<Service>>
    suspend fun getServiceById(id: String): Result<Service>
}
