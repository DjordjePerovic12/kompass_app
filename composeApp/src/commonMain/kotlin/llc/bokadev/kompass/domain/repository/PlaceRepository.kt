package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory

interface PlaceRepository {
    suspend fun getActivePlaces(): Resource<List<Place>, NetworkError, String?>
    suspend fun getMustSeePlaces(): Resource<List<Place>, NetworkError, String?>
    suspend fun getPlacesByCategory(category: PlaceCategory): Resource<List<Place>, NetworkError, String?>
    suspend fun getPlaceById(id: String): Resource<Place, NetworkError, String?>
}
