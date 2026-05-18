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
        favoritesModule,
        searchModule,
        placesListModule,
        nearbyPlacesModule,
        placeDetailModule,
        categoryModule,
        premiumModule,
        localFindsModule
    )
}
