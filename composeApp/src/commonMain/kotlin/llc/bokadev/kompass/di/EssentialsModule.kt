package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.EssentialRepositoryImpl
import llc.bokadev.kompass.data.repository.UtilityRepositoryImpl
import llc.bokadev.kompass.domain.repository.EssentialRepository
import llc.bokadev.kompass.domain.repository.UtilityRepository
import llc.bokadev.kompass.domain.usecase.GetEssentialsUseCase
import llc.bokadev.kompass.domain.usecase.GetUtilitiesUseCase
import llc.bokadev.kompass.presentation.screens.essentials.EssentialsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val essentialsModule = module {
    single<EssentialRepository> { EssentialRepositoryImpl(get(), get()) }
    single<UtilityRepository> { UtilityRepositoryImpl(get(), get()) }
    factory { GetEssentialsUseCase(get()) }
    factory { GetUtilitiesUseCase(get()) }
    viewModel { EssentialsViewModel(get(), get(), get()) }
}
