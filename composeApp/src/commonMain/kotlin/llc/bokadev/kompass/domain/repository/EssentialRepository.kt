package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.CityEssential

interface EssentialRepository {
    suspend fun getEssentials(): Result<List<CityEssential>>
}
