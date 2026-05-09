package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.PlaceRepositoryImpl
import llc.bokadev.kompass.domain.repository.PlaceRepository
import llc.bokadev.kompass.domain.usecase.GetNearbyPlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetMustSeePlacesUseCase
import llc.bokadev.kompass.domain.usecase.GetPlaceByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetPlacesByCategoryUseCase
import org.koin.dsl.module

val placeModule = module {
    single<PlaceRepository> { PlaceRepositoryImpl(get(), get()) }
    factory { GetNearbyPlacesUseCase(get()) }
    factory { GetMustSeePlacesUseCase(get()) }
    factory { GetPlacesByCategoryUseCase(get()) }
    factory { GetPlaceByIdUseCase(get()) }
}
