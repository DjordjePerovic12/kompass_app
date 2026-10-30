package llc.bokadev.kompass.di

import llc.bokadev.kompass.domain.usecase.GetNearbyFallbackPlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetDestinationOverviewUseCase
import llc.bokadev.kompass.presentation.screens.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    single { GetNearbyFallbackPlacesUseCase(get()) }
    single { GetDestinationOverviewUseCase(get()) }
    viewModelOf(::HomeViewModel)
}
