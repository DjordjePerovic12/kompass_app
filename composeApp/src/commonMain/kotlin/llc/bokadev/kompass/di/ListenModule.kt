package llc.bokadev.kompass.di

import llc.bokadev.kompass.domain.usecase.GetAudioCatalogUseCase
import llc.bokadev.kompass.presentation.listen.ListenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val listenModule = module {
    factory { GetAudioCatalogUseCase(get(), get(), get()) }
    viewModelOf(::ListenViewModel)
}
