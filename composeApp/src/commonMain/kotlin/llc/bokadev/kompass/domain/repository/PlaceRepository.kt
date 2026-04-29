package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.Place

interface PlaceRepository {
    suspend fun getMustSeePlaces(): Result<List<Place>>
}
