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
import llc.bokadev.kompass.data.remote.dto.CityEssentialDto
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.repository.EssentialRepository

class EssentialRepositoryImpl(
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : EssentialRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getEssentials(): Resource<List<CityEssential>, NetworkError, String?> =
        getEssentialDtos()
            .mapCatching { essentials -> essentials.map { it.toDomain() } }

    private suspend fun getEssentialDtos(): Resource<List<CityEssentialDto>, NetworkError, String?> =
        safeSupabaseCall {
            supabase.from("city_essentials")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<CityEssentialDto>()
                .also { remoteEssentials -> cacheEssentials(remoteEssentials) }
        }.recover { _, _ ->
            readCachedEssentials().ifEmpty { null }
        }

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
