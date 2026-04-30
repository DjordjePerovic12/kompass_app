package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.CategoryDto
import llc.bokadev.kompass.domain.model.Category
import llc.bokadev.kompass.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val supabase: SupabaseClient
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        supabase.from("categories")
            .select {
                filter { eq("is_active", true) }
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<CategoryDto>()
            .map { it.toDomain() }
    }
}
