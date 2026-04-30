package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.nearby.NearbyPlacesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val nearbyPlacesModule = module {
    viewModel { NearbyPlacesViewModel(get(), get()) }
}
