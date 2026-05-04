package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.Experience

interface ExperienceRepository {
    suspend fun getActivities(): Result<List<Experience>>
    suspend fun getActivityById(id: String): Result<Experience>
}
