package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.EssentialRepositoryImpl
import llc.bokadev.kompass.domain.repository.EssentialRepository
import llc.bokadev.kompass.domain.usecase.GetEssentialsUseCase
import llc.bokadev.kompass.presentation.screens.essentials.EssentialsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val essentialsModule = module {
    single<EssentialRepository> { EssentialRepositoryImpl(get(), get()) }
    factory { GetEssentialsUseCase(get()) }
    viewModel { EssentialsViewModel(get()) }
}
