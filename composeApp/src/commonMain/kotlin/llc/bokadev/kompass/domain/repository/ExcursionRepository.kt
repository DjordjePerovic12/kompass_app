package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.Excursion
import llc.bokadev.kompass.domain.model.ExcursionResult
import llc.bokadev.kompass.domain.model.Experience

interface ExcursionRepository {
    suspend fun getCollection(): Result<ExcursionResult<Excursion>>
    suspend fun getRelatedActivities(placeId: String): Result<ExcursionResult<Experience>>
}
