package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.LocalFindRepositoryImpl
import llc.bokadev.kompass.domain.repository.LocalFindRepository
import llc.bokadev.kompass.domain.usecase.GetLocalFindByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetLocalFindsUseCase
import llc.bokadev.kompass.presentation.screens.localfinds.LocalFindDetailViewModel
import llc.bokadev.kompass.presentation.screens.localfinds.LocalFindsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val localFindsModule = module {
    single<LocalFindRepository> { LocalFindRepositoryImpl(get(), get()) }
    factory { GetLocalFindsUseCase(get()) }
    factory { GetLocalFindByIdUseCase(get()) }
    viewModel { LocalFindsViewModel(get(), get(), get(), get()) }
    viewModel { (id: String, filter: String?) -> LocalFindDetailViewModel(id, filter, get(), get(), get(), get(), get(), get()) }
}
