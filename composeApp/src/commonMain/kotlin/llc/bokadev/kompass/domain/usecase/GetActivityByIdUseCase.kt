package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.repository.ExperienceRepository

class GetActivityByIdUseCase(
    private val repository: ExperienceRepository
) {
    suspend operator fun invoke(id: String): Result<Experience> = repository.getActivityById(id)
}
