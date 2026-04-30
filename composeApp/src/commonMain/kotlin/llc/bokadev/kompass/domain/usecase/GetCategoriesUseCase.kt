package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Category
import llc.bokadev.kompass.domain.repository.CategoryRepository

class GetCategoriesUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(): Result<List<Category>> = repository.getCategories()
}
