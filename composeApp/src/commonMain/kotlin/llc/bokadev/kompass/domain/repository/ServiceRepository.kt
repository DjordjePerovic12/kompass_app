package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Service

interface ServiceRepository {
    suspend fun getServices(): Resource<List<Service>, NetworkError, String?>
    suspend fun getServiceById(id: String): Resource<Service, NetworkError, String?>
}
