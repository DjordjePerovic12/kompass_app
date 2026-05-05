package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.repository.InfoCenterRepository

class GetCurrentInfoNoticesUseCase(
    private val repository: InfoCenterRepository
) {
    suspend operator fun invoke(): Result<List<InfoNotice>> = repository.getCurrentNotices()
}
