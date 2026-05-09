package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.InfoNoticeDto
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.repository.InfoCenterRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class InfoCenterRepositoryImpl(
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : InfoCenterRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getCurrentNotices(): Result<List<InfoNotice>> = runCatching {
        getActiveNoticeDtos()
            .map { it.toDomain() }
            .filter { it.isCurrent() }
            .sortedWith(
                compareByDescending<InfoNotice> { it.recencyKey() }
                    .thenBy { it.priorityRank() }
                    .thenBy { it.sortOrder }
            )
    }

    override suspend fun getNoticeById(id: String): Result<InfoNotice> = runCatching {
        runCatching {
            supabase.from("info_center")
                .select { filter { eq("id", id) } }
                .decodeSingle<InfoNoticeDto>()
        }.fold(
            onSuccess = { remoteNotice ->
                mergeNoticeIntoCache(remoteNotice)
                remoteNotice
            },
            onFailure = { remoteError ->
                readCachedNotices().firstOrNull { it.id == id } ?: throw remoteError
            }
        ).toDomain()
    }

    private suspend fun getActiveNoticeDtos(): List<InfoNoticeDto> =
        runCatching {
            supabase.from("info_center")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<InfoNoticeDto>()
        }.fold(
            onSuccess = { remoteNotices ->
                cacheNotices(remoteNotices)
                remoteNotices
            },
            onFailure = { remoteError ->
                val cachedNotices = readCachedNotices()
                if (cachedNotices.isNotEmpty()) cachedNotices else throw remoteError
            }
        )

    private fun cacheNotices(dtos: List<InfoNoticeDto>) {
        appPreferences.setString(
            CACHE_KEY_INFO_NOTICES,
            json.encodeToString(ListSerializer(InfoNoticeDto.serializer()), dtos)
        )
    }

    private fun readCachedNotices(): List<InfoNoticeDto> {
        val raw = appPreferences.getString(CACHE_KEY_INFO_NOTICES) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(InfoNoticeDto.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    private fun mergeNoticeIntoCache(dto: InfoNoticeDto) {
        val merged = readCachedNotices()
            .filterNot { it.id == dto.id }
            .plus(dto)
            .sortedByDescending { it.createdAt ?: "" }
        cacheNotices(merged)
    }

    private companion object {
        const val CACHE_KEY_INFO_NOTICES = "offline_cache_info_notices_v1"
    }
}
