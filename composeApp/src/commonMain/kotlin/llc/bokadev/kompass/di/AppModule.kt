package llc.bokadev.kompass.di

import org.koin.dsl.module

val appModule = module {
    includes(
        platformModule,
        networkModule,
        languagePickerModule,
        homeModule,
        categoriesModule,
        eventsModule,
        experiencesModule,
        essentialsModule
    )
}
