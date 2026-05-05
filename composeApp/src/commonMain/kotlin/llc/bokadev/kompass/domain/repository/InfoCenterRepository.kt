package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.InfoNotice

interface InfoCenterRepository {
    suspend fun getCurrentNotices(): Result<List<InfoNotice>>
    suspend fun getNoticeById(id: String): Result<InfoNotice>
}
