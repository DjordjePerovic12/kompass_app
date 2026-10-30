package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.LocalFind

interface LocalFindRepository {
    suspend fun getLocalFinds(): Resource<List<LocalFind>, NetworkError, String?>
    suspend fun getLocalFindById(id: String): Resource<LocalFind, NetworkError, String?>
}
