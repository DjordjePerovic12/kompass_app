package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.CityEssentialDto
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.repository.EssentialRepository

class EssentialRepositoryImpl(
    private val supabase: SupabaseClient
) : EssentialRepository {

    override suspend fun getEssentials(): Result<List<CityEssential>> = runCatching {
        supabase.from("city_essentials")
            .select {
                filter { eq("is_active", true) }
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<CityEssentialDto>()
            .map { it.toDomain() }
    }
}
