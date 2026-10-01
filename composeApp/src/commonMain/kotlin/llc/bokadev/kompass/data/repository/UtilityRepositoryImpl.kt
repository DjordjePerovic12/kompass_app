package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.UtilityDto
import llc.bokadev.kompass.domain.model.Utility
import llc.bokadev.kompass.domain.repository.UtilityRepository

class UtilityRepositoryImpl(
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : UtilityRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getUtilities(): Result<List<Utility>> = runCatching {
        getUtilityDtos().map { it.toDomain() }
    }

    private suspend fun getUtilityDtos(): List<UtilityDto> =
        runCatching {
            supabase.from("utilities")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<UtilityDto>()
        }.fold(
            onSuccess = { remoteUtilities ->
                cacheUtilities(remoteUtilities)
                remoteUtilities
            },
            onFailure = { remoteError ->
                val cachedUtilities = readCachedUtilities()
                if (cachedUtilities.isNotEmpty()) cachedUtilities else throw remoteError
            }
        )

    private fun cacheUtilities(dtos: List<UtilityDto>) {
        appPreferences.setString(
            CACHE_KEY_UTILITIES,
            json.encodeToString(ListSerializer(UtilityDto.serializer()), dtos)
        )
    }

    private fun readCachedUtilities(): List<UtilityDto> {
        val raw = appPreferences.getString(CACHE_KEY_UTILITIES) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(UtilityDto.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    private companion object {
        const val CACHE_KEY_UTILITIES = "offline_cache_utilities_v1"
    }
}
