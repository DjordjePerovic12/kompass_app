package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}
