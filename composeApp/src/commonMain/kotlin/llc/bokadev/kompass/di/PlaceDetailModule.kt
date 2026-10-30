package llc.bokadev.kompass.di

import llc.bokadev.kompass.presentation.placedetail.PlaceDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val placeDetailModule = module {
    viewModelOf(::PlaceDetailViewModel)
}
