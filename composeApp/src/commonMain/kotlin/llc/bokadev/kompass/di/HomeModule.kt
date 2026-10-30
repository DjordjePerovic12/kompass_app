package llc.bokadev.kompass.di

import llc.bokadev.kompass.domain.usecase.GetNearbyFallbackPlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetDestinationOverviewUseCase
import llc.bokadev.kompass.presentation.screens.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    single { GetNearbyFallbackPlacesUseCase(get()) }
    single { GetDestinationOverviewUseCase(get()) }
    viewModel {
        HomeViewModel(
            getMustSeePlaces = get(),
            getUpcomingEvents = get(),
            getCurrentInfoNotices = get(),
            getNearbyPlaces = get(),
            getNearbyFallbackPlaces = get(),
            getDestinationOverview = get(),
            userLocationProvider = get(),
            preferences = get(),
            analytics = get(),
            favorites = get(),
            navigator = get(),
            languageSheet = get()
        )
    }
}
