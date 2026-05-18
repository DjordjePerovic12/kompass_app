package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.EventRepositoryImpl
import llc.bokadev.kompass.domain.repository.EventRepository
import llc.bokadev.kompass.domain.usecase.GetEventByIdUseCase
import llc.bokadev.kompass.domain.usecase.GetEventFiltersUseCase
import llc.bokadev.kompass.domain.usecase.GetEventsUseCase
import llc.bokadev.kompass.domain.usecase.GetUpcomingEventsUseCase
import llc.bokadev.kompass.presentation.screens.eventdetail.EventDetailViewModel
import llc.bokadev.kompass.presentation.screens.events.EventsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val eventsModule = module {
    single<EventRepository> { EventRepositoryImpl(get(), get()) }
    factory { GetEventsUseCase(get()) }
    factory { GetEventFiltersUseCase(get()) }
    factory { GetUpcomingEventsUseCase(get()) }
    factory { GetEventByIdUseCase(get()) }
    viewModel { EventsViewModel(get(), get()) }
    viewModel { (id: String) -> EventDetailViewModel(id, get()) }
}
