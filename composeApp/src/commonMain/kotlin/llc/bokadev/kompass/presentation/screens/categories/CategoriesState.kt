package llc.bokadev.kompass.presentation.screens.categories

import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.domain.model.Category

data class CategoriesState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val categories: List<Category> = emptyList()
) : BaseState()
