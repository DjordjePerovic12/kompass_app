package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.CategoryDto
import llc.bokadev.kompass.domain.model.Category
import llc.bokadev.kompass.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : CategoryRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        runCatching {
            supabase.from("categories")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<CategoryDto>()
        }.fold(
            onSuccess = { remoteCategories ->
                cacheCategories(remoteCategories)
                remoteCategories
            },
            onFailure = { remoteError ->
                val cachedCategories = readCachedCategories()
                if (cachedCategories.isNotEmpty()) cachedCategories else throw remoteError
            }
        ).map { it.toDomain() }
    }

    private fun cacheCategories(dtos: List<CategoryDto>) {
        appPreferences.setString(
            CACHE_KEY_ACTIVE_CATEGORIES,
            json.encodeToString(ListSerializer(CategoryDto.serializer()), dtos)
        )
    }

    private fun readCachedCategories(): List<CategoryDto> {
        val raw = appPreferences.getString(CACHE_KEY_ACTIVE_CATEGORIES) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(CategoryDto.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    private companion object {
        const val CACHE_KEY_ACTIVE_CATEGORIES = "offline_cache_active_categories_v1"
    }
}
