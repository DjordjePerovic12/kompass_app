package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.CategoryRepositoryImpl
import llc.bokadev.kompass.domain.repository.CategoryRepository
import llc.bokadev.kompass.domain.usecase.GetCategoriesUseCase
import llc.bokadev.kompass.presentation.screens.categories.CategoriesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoryModule = module {
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    factory { GetCategoriesUseCase(get()) }
    viewModel { CategoriesViewModel(get()) }
}
