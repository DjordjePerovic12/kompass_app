package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.LocalFind

interface LocalFindRepository {
    suspend fun getLocalFinds(): Result<List<LocalFind>>
    suspend fun getLocalFindById(id: String): Result<LocalFind>
}
