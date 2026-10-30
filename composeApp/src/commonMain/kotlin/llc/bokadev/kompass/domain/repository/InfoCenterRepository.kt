package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.InfoNotice

interface InfoCenterRepository {
    suspend fun getCurrentNotices(): Resource<List<InfoNotice>, NetworkError, String?>
    suspend fun getNoticeById(id: String): Resource<InfoNotice, NetworkError, String?>
}
