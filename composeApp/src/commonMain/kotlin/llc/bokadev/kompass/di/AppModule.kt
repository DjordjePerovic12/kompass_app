package llc.bokadev.kompass.di

import org.koin.dsl.module

val appModule = module {
    includes(
        platformModule,
        networkModule,
        placeModule,
        languagePickerModule,
        homeModule,
        categoriesModule,
        eventsModule,
        infoCenterModule,
        experiencesModule,
        essentialsModule,
        servicesModule,
        analyticsModule,
        placesListModule,
        nearbyPlacesModule,
        placeDetailModule,
        categoryModule,
        premiumModule
    )
}
