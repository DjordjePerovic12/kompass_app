package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.repository.InfoCenterRepository

class GetInfoNoticeByIdUseCase(
    private val repository: InfoCenterRepository
) {
    suspend operator fun invoke(id: String): Result<InfoNotice> = repository.getNoticeById(id)
}
