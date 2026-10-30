package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Utility

interface UtilityRepository {
    suspend fun getUtilities(): Resource<List<Utility>, NetworkError, String?>
}
