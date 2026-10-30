package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.ExcursionDto
import llc.bokadev.kompass.data.remote.dto.PlaceActivityDto
import llc.bokadev.kompass.domain.model.ExcursionResult
import llc.bokadev.kompass.domain.repository.ExcursionRepository

class ExcursionRepositoryImpl(
    private val supabase: SupabaseClient,
    private val preferences: AppPreferences,
    private val citySlug: String = "kotor"
) : ExcursionRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getCollection() = cached("collection", ExcursionDto.serializer()) {
        supabase.from("worth_the_drive_items").select(
            Columns.raw("*,place:places(*),activity:experiences(*),city:cities!inner(slug)")
        ) { filter { eq("is_active", true); eq("city.slug", citySlug) } }.decodeList<ExcursionDto>()
    }.map { result ->
        ExcursionResult(result.items.mapNotNull { it.toDomainOrNull() }.sortedWith(compareBy({ it.sortOrder }, { it.id })), result.isCached)
    }

    override suspend fun getRelatedActivities(placeId: String) = cached("related_$placeId", PlaceActivityDto.serializer()) {
        supabase.from("place_activities").select(
            Columns.raw("*,activity:experiences(*),city:cities!inner(slug)")
        ) { filter { eq("is_active", true); eq("place_id", placeId); eq("city.slug", citySlug) } }.decodeList<PlaceActivityDto>()
    }.map { result ->
        ExcursionResult(result.items.filter { it.isActive }.sortedWith(compareBy({ it.sortOrder }, { it.id }))
            .mapNotNull { it.activity?.takeIf { activity -> activity.isActive }?.toDomain() }, result.isCached)
    }

    private suspend fun <T> cached(key: String, serializer: KSerializer<T>, fetch: suspend () -> List<T>): Result<ExcursionResult<T>> {
        val cacheKey = "excursions_v1_${citySlug}_$key"
        val listSerializer = ListSerializer(serializer)
        return try {
            val data = fetch()
            preferences.setString(cacheKey, json.encodeToString(listSerializer, data))
            Result.success(ExcursionResult(data))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            val cached = preferences.getString(cacheKey)?.let { raw ->
                runCatching { json.decodeFromString(listSerializer, raw) }.getOrNull()
            }
            if (cached != null) Result.success(ExcursionResult(cached, isCached = true)) else Result.failure(error)
        }
    }
}
