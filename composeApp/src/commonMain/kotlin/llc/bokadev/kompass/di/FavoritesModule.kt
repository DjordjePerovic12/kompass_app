package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.FavoritesRepositoryImpl
import llc.bokadev.kompass.data.repository.SavedSnapshotRepositoryImpl
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.domain.repository.SavedSnapshotRepository
import llc.bokadev.kompass.presentation.favorites.FavoritesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val favoritesModule = module {
    single<FavoritesRepository> { FavoritesRepositoryImpl(get()) }
    single<SavedSnapshotRepository> { SavedSnapshotRepositoryImpl(get()) }
    viewModelOf(::FavoritesViewModel)
}
