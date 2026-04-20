package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.categories.CategoriesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoriesModule = module {
    viewModel { CategoriesViewModel() }
}
