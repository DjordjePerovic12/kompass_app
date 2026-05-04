package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.ExperienceDto
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.repository.ExperienceRepository

class ExperienceRepositoryImpl(
    private val supabase: SupabaseClient
) : ExperienceRepository {

    override suspend fun getActivities(): Result<List<Experience>> = runCatching {
        supabase.from("experiences")
            .select {
                filter { eq("is_active", true) }
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<ExperienceDto>()
            .map { it.toDomain() }
    }

    override suspend fun getActivityById(id: String): Result<Experience> = runCatching {
        supabase.from("experiences")
            .select { filter { eq("id", id) } }
            .decodeSingle<ExperienceDto>()
            .toDomain()
    }
}
