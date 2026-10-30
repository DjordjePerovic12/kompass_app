package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.InfoCenterRepositoryImpl
import llc.bokadev.kompass.domain.repository.InfoCenterRepository
import llc.bokadev.kompass.domain.usecase.GetCurrentInfoNoticesUseCase
import llc.bokadev.kompass.domain.usecase.GetInfoNoticeByIdUseCase
import llc.bokadev.kompass.presentation.infocenter.detail.InfoCenterDetailViewModel
import llc.bokadev.kompass.presentation.infocenter.InfoCenterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val infoCenterModule = module {
    single<InfoCenterRepository> { InfoCenterRepositoryImpl(get(), get()) }
    factory { GetCurrentInfoNoticesUseCase(get()) }
    factory { GetInfoNoticeByIdUseCase(get()) }
    viewModelOf(::InfoCenterViewModel)
    viewModelOf(::InfoCenterDetailViewModel)
}
