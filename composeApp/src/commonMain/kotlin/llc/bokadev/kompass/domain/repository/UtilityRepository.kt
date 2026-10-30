package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.Utility

interface UtilityRepository {
    suspend fun getUtilities(): Result<List<Utility>>
}
