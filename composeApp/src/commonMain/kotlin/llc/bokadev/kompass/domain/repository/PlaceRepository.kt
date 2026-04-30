package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory

interface PlaceRepository {
    suspend fun getActivePlaces(): Result<List<Place>>
    suspend fun getMustSeePlaces(): Result<List<Place>>
    suspend fun getPlacesByCategory(category: PlaceCategory): Result<List<Place>>
    suspend fun getPlaceById(id: String): Result<Place>
}
