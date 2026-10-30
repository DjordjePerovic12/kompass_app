package llc.bokadev.kompass.di

import llc.bokadev.kompass.core.utils.SearchVocabularyTable
import llc.bokadev.kompass.data.repository.SearchRepositoryImpl
import llc.bokadev.kompass.domain.model.SearchVocabulary
import llc.bokadev.kompass.data.repository.RecentSearchesStore
import llc.bokadev.kompass.domain.repository.RecentSearchesRepository
import llc.bokadev.kompass.domain.repository.SearchRepository
import llc.bokadev.kompass.domain.usecase.BuildSearchIndexUseCase
import llc.bokadev.kompass.presentation.screens.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val searchModule = module {
    single<SearchRepository> { SearchRepositoryImpl(get(named("kompassApi")), get()) }
    single<RecentSearchesRepository> { RecentSearchesStore(get()) }
    single<SearchVocabulary> { SearchVocabularyTable() }
    factory { BuildSearchIndexUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel {
        val buildIndex = get<BuildSearchIndexUseCase>()
        SearchViewModel({ buildIndex(it) }, get(), get(), get(), get())
    }
}
