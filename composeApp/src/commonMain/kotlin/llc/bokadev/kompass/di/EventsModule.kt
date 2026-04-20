package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.events.EventsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val eventsModule = module {
    viewModel { EventsViewModel() }
}
