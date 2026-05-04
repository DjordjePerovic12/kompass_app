package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.repository.ExperienceRepository

class GetActivitiesUseCase(
    private val repository: ExperienceRepository
) {
    suspend operator fun invoke(): Result<List<Experience>> = repository.getActivities()
}
