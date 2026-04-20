package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.experiences.ExperiencesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val experiencesModule = module {
    viewModel { ExperiencesViewModel() }
}
