package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.category_items_list.CategoryItemsListViewModel
import llc.bokadev.kompass.presentation.screens.mustsee.MustSeePlacesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val placesListModule = module {
    viewModel { CategoryItemsListViewModel(get()) }
    viewModel { MustSeePlacesViewModel(get()) }
}
