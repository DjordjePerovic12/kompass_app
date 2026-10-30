package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Resource<List<Category>, NetworkError, String?>
}
