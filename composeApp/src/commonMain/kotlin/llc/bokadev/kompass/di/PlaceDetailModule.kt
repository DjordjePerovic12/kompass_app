package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.placedetail.PlaceDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val placeDetailModule = module {
    viewModel { PlaceDetailViewModel(get()) }
}
