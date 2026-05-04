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
        experiencesModule,
        essentialsModule,
        servicesModule,
        placesListModule,
        nearbyPlacesModule,
        placeDetailModule,
        categoryModule,
        premiumModule
    )
}
