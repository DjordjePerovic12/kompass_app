package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.screens.placedetail.PlaceDetailViewModel
import llc.bokadev.kompass.presentation.screens.placedetail.PlaceGuideViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val placeDetailModule = module {
    viewModel { PlaceDetailViewModel(get(), get()) }
    viewModel { (id: String, autoplay: Boolean, deep: Boolean) -> PlaceGuideViewModel(id, autoplay, deep, get(), get(), get(), get(), get(), get()) }
}
