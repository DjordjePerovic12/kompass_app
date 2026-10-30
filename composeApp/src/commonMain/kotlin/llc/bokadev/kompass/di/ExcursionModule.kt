package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.ExcursionRepositoryImpl
import llc.bokadev.kompass.domain.repository.ExcursionRepository
import llc.bokadev.kompass.presentation.excursions.ExcursionsViewModel
import llc.bokadev.kompass.presentation.excursions.related_activities.RelatedActivitiesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val excursionModule = module {
    single<ExcursionRepository> { ExcursionRepositoryImpl(get(), get()) }
    viewModelOf(::ExcursionsViewModel)
    // Takes the place id via parametersOf (see RelatedActivities).
    viewModelOf(::RelatedActivitiesViewModel)
}
