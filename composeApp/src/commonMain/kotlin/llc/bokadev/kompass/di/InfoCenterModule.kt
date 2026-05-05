package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.InfoCenterRepositoryImpl
import llc.bokadev.kompass.domain.repository.InfoCenterRepository
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.domain.usecase.GetInfoNoticeByIdUseCase
import llc.bokadev.kompass.presentation.screens.infocenter.InfoCenterDetailViewModel
import llc.bokadev.kompass.presentation.screens.infocenter.InfoCenterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val infoCenterModule = module {
    single<InfoCenterRepository> { InfoCenterRepositoryImpl(get()) }
    factory { GetCurrentInfoNoticesUseCase(get()) }
    factory { GetInfoNoticeByIdUseCase(get()) }
    viewModel { InfoCenterViewModel(get()) }
    viewModel { (id: String) -> InfoCenterDetailViewModel(id, get()) }
}
