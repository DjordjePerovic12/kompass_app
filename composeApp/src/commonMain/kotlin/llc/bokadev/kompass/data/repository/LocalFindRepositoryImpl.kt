package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.LocalFindDto
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.domain.repository.LocalFindRepository

class LocalFindRepositoryImpl(
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : LocalFindRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getLocalFinds(): Result<List<LocalFind>> = runCatching {
        runCatching {
            supabase.from("local_finds")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<LocalFindDto>()
        }.fold(
            onSuccess = { remote ->
                cacheLocalFinds(remote)
                remote
            },
            onFailure = { remoteError ->
                val cached = readCachedLocalFinds()
                if (cached.isNotEmpty()) cached else throw remoteError
            }
        ).map { it.toDomain() }
    }

    override suspend fun getLocalFindById(id: String): Result<LocalFind> = runCatching {
        getLocalFinds().getOrThrow().firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Local find not found: $id")
    }

    private fun cacheLocalFinds(dtos: List<LocalFindDto>) {
        appPreferences.setString(
            CACHE_KEY,
            json.encodeToString(ListSerializer(LocalFindDto.serializer()), dtos)
        )
    }

    private fun readCachedLocalFinds(): List<LocalFindDto> {
        val raw = appPreferences.getString(CACHE_KEY) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(LocalFindDto.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    private companion object {
        const val CACHE_KEY = "offline_cache_local_finds_v1"
    }
}
