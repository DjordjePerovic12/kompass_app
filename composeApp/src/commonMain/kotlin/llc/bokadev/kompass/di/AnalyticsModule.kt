package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.repository.AnalyticsRepositoryImpl
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val analyticsModule = module {
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get(named("kompassApi")), get()) }
}
