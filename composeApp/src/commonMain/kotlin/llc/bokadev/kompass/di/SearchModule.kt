package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.SearchRepositoryImpl
import llc.bokadev.kompass.domain.repository.SearchRepository
import llc.bokadev.kompass.presentation.screens.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val searchModule = module {
    single<SearchRepository> { SearchRepositoryImpl(get(named("kompassApi")), get()) }
    viewModel { SearchViewModel(get(), get(), get()) }
}
