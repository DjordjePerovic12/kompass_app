package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Experience

interface ExperienceRepository {
    suspend fun getActivities(): Resource<List<Experience>, NetworkError, String?>
    suspend fun getActivityById(id: String): Resource<Experience, NetworkError, String?>
}
