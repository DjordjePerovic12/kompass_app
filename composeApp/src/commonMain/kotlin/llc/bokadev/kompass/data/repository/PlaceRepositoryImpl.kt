package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.PlaceDto
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.repository.PlaceRepository

class PlaceRepositoryImpl(
    private val supabase: SupabaseClient
) : PlaceRepository {

    override suspend fun getMustSeePlaces(): Result<List<Place>> = runCatching {
        supabase.from("places")
            .select {
                filter {
                    eq("category", "see_and_visit")
                    eq("is_active", true)
                }
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<PlaceDto>()
            .map { it.toDomain() }
    }
}
