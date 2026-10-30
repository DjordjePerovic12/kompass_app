package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.repository.ExperienceRepository

class GetActivitiesUseCase(
    private val repository: ExperienceRepository
) {
    suspend operator fun invoke(): Resource<List<Experience>, NetworkError, String?> = repository.getActivities()
}
