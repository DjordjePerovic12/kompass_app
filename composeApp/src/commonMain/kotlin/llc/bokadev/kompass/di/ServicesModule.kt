package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.ServiceRepositoryImpl
import llc.bokadev.kompass.domain.repository.ServiceRepository
import llc.bokadev.kompass.domain.usecase.GetServiceByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetServicesUseCase
import llc.bokadev.kompass.presentation.screens.services.ServiceDetailViewModel
import llc.bokadev.kompass.presentation.screens.services.ServicesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val servicesModule = module {
    single<ServiceRepository> { ServiceRepositoryImpl(get()) }
    factory { GetServicesUseCase(get()) }
    factory { GetServiceByIdUseCase(get()) }
    viewModel { ServicesViewModel(get()) }
    viewModel { (id: String) -> ServiceDetailViewModel(id, get()) }
}
