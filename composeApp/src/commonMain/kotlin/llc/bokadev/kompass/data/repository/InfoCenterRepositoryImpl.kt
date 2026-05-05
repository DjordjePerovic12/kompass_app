package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.InfoNoticeDto
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.repository.InfoCenterRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class InfoCenterRepositoryImpl(
    private val supabase: SupabaseClient
) : InfoCenterRepository {

    override suspend fun getCurrentNotices(): Result<List<InfoNotice>> = runCatching {
        supabase.from("info_center")
            .select {
                filter { eq("is_active", true) }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<InfoNoticeDto>()
            .map { it.toDomain() }
            .filter { it.isCurrent() }
            .sortedWith(
                compareByDescending<InfoNotice> { it.recencyKey() }
                    .thenBy { it.priorityRank() }
                    .thenBy { it.sortOrder }
            )
    }

    override suspend fun getNoticeById(id: String): Result<InfoNotice> = runCatching {
        supabase.from("info_center")
            .select { filter { eq("id", id) } }
            .decodeSingle<InfoNoticeDto>()
            .toDomain()
    }
}
