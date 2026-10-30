package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.CityEssential

interface EssentialRepository {
    suspend fun getEssentials(): Resource<List<CityEssential>, NetworkError, String?>
}
