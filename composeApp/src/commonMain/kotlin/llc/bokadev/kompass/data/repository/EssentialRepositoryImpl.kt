package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.CityEssentialDto
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.repository.EssentialRepository

class EssentialRepositoryImpl(
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : EssentialRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getEssentials(): Result<List<CityEssential>> = runCatching {
        getEssentialDtos()
            .map { it.toDomain() }
    }

    private suspend fun getEssentialDtos(): List<CityEssentialDto> =
        runCatching {
            supabase.from("city_essentials")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<CityEssentialDto>()
        }.fold(
            onSuccess = { remoteEssentials ->
                cacheEssentials(remoteEssentials)
                remoteEssentials
            },
            onFailure = { remoteError ->
                val cachedEssentials = readCachedEssentials()
                if (cachedEssentials.isNotEmpty()) cachedEssentials else throw remoteError
            }
        )

    private fun cacheEssentials(dtos: List<CityEssentialDto>) {
        appPreferences.setString(
            CACHE_KEY_CITY_ESSENTIALS,
            json.encodeToString(ListSerializer(CityEssentialDto.serializer()), dtos)
        )
    }

    private fun readCachedEssentials(): List<CityEssentialDto> {
        val raw = appPreferences.getString(CACHE_KEY_CITY_ESSENTIALS) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(CityEssentialDto.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    private companion object {
        const val CACHE_KEY_CITY_ESSENTIALS = "offline_cache_city_essentials_v1"
    }
}
