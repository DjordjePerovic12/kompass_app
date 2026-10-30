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

    override suspend fun getCurrentNotices(): Resource<List<InfoNotice>, NetworkError, String?> =
        getActiveNoticeDtos().mapCatching { notices ->
            notices
                .map { it.toDomain() }
                .filter { it.isCurrent() }
                .sortedWith(
                    compareByDescending<InfoNotice> { it.recencyKey() }
                        .thenBy { it.priorityRank() }
                        .thenBy { it.sortOrder }
                )
        }

    override suspend fun getNoticeById(id: String): Resource<InfoNotice, NetworkError, String?> =
        safeSupabaseCall {
            supabase.from("info_center")
                .select { filter { eq("id", id) } }
                .decodeSingle<InfoNoticeDto>()
                .also { remoteNotice -> mergeNoticeIntoCache(remoteNotice) }
        }.recover { _, _ ->
            readCachedNotices().firstOrNull { it.id == id }
        }.mapCatching { it.toDomain() }

    private suspend fun getActiveNoticeDtos(): Resource<List<InfoNoticeDto>, NetworkError, String?> =
        safeSupabaseCall {
            supabase.from("info_center")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<InfoNoticeDto>()
                .also { remoteNotices -> cacheNotices(remoteNotices) }
        }.recover { _, _ ->
            readCachedNotices().ifEmpty { null }
        }

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
