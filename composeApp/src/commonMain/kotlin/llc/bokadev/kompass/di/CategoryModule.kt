package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.explore.ExploreViewModel
import llc.bokadev.kompass.presentation.categorylist.CategoryListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val categoryModule = module {
    viewModelOf(::ExploreViewModel)
    viewModel { params -> CategoryListViewModel(get(), get(), get(), get(), get(), get(), get(), params.getOrNull<String>()) }
}
