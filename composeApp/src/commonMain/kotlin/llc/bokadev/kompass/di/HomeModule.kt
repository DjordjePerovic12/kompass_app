package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
}
