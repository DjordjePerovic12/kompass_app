package llc.bokadev.kompass.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import llc.bokadev.kompass.core.audio.AudioController
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.navigation.utils.NavigatorImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import llc.bokadev.kompass.core.utils.PlaybackRequestGuard
import llc.bokadev.kompass.domain.repository.AudioGuideRepository
import llc.bokadev.kompass.presentation.shared.LanguageSheetHost
import org.koin.dsl.module

val appModule = module {
    single { PlaybackRequestGuard() }
    singleOf(::NavigatorImpl) { bind<Navigator>() }
    single { SnackbarBus() }
    single { NavAudioHost() }
    single { LanguageSheetHost() }
    single {
        // Stream-first: playing never downloads; downloads are explicit (audio library).
        val audioRepository = get<AudioGuideRepository>()
        AudioController(
            player = get(),
            guard = get(),
            resolveUrl = { path -> audioRepository.streamUrl(path) },
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        )
    }
    includes(
        platformModule,
        stringProviderModule,
        networkModule,
        placeModule,
        excursionModule,
        languagePickerModule,
        homeModule,
        eventsModule,
        infoCenterModule,
        experiencesModule,
        essentialsModule,
        servicesModule,
        analyticsModule,
        favoritesModule,
        searchModule,
        placeDetailModule,
        categoryModule,
        listenModule,
        localFindsModule
    )
}
