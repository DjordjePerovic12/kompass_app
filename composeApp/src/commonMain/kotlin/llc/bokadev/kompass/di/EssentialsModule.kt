package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.essentials.EssentialsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val essentialsModule = module {
    viewModel { EssentialsViewModel() }
}
