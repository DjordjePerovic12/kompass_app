package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.mapCatching
import llc.bokadev.kompass.core.utils.networking.recover
import llc.bokadev.kompass.core.utils.networking.safeSupabaseCall
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.ExperienceDto
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.repository.ExperienceRepository

class ExperienceRepositoryImpl(
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : ExperienceRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getActivities(): Resource<List<Experience>, NetworkError, String?> =
        getActiveExperienceDtos()
            .mapCatching { experiences -> experiences.map { it.toDomain() } }

    override suspend fun getActivityById(id: String): Resource<Experience, NetworkError, String?> =
        safeSupabaseCall {
            supabase.from("experiences")
                .select { filter { eq("id", id) } }
                .decodeSingle<ExperienceDto>()
                .also { remoteActivity -> mergeExperienceIntoCache(remoteActivity) }
        }.recover { _, _ ->
            readCachedExperiences().firstOrNull { it.id == id }
        }.mapCatching { it.toDomain() }

    private suspend fun getActiveExperienceDtos(): Resource<List<ExperienceDto>, NetworkError, String?> =
        safeSupabaseCall {
            supabase.from("experiences")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<ExperienceDto>()
                .also { remoteExperiences -> cacheExperiences(remoteExperiences) }
        }.recover { _, _ ->
            readCachedExperiences().ifEmpty { null }
        }

    private fun cacheExperiences(dtos: List<ExperienceDto>) {
        appPreferences.setString(
            CACHE_KEY_ACTIVE_EXPERIENCES,
            json.encodeToString(ListSerializer(ExperienceDto.serializer()), dtos)
        )
    }

    private fun readCachedExperiences(): List<ExperienceDto> {
        val raw = appPreferences.getString(CACHE_KEY_ACTIVE_EXPERIENCES) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(ExperienceDto.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    private fun mergeExperienceIntoCache(dto: ExperienceDto) {
        val merged = readCachedExperiences()
            .filterNot { it.id == dto.id }
            .plus(dto)
            .sortedBy { it.sortOrder }
        cacheExperiences(merged)
    }

    private companion object {
        const val CACHE_KEY_ACTIVE_EXPERIENCES = "offline_cache_active_experiences_v1"
    }
}
