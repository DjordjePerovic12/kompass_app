package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Excursion
import llc.bokadev.kompass.domain.model.ExcursionResult
import llc.bokadev.kompass.domain.model.Experience

interface ExcursionRepository {
    suspend fun getCollection(): Resource<ExcursionResult<Excursion>, NetworkError, String?>
    suspend fun getRelatedActivities(placeId: String): Resource<ExcursionResult<Experience>, NetworkError, String?>
}
