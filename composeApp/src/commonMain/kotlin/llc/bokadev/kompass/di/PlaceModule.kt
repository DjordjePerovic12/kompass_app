package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.PlaceRepositoryImpl
import llc.bokadev.kompass.domain.repository.PlaceRepository
import llc.bokadev.kompass.domain.usecase.GetMustSeePlacesUseCase
import org.koin.dsl.module

val placeModule = module {
    single<PlaceRepository> { PlaceRepositoryImpl(get()) }
    factory { GetMustSeePlacesUseCase(get()) }
}
