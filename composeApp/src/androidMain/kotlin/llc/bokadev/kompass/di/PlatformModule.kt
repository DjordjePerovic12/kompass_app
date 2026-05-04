package llc.bokadev.kompass.di

import llc.bokadev.kompass.core.util.AndroidAppPreferences
import llc.bokadev.kompass.core.util.AndroidAudioGuidePlayer
import llc.bokadev.kompass.core.util.AudioGuidePlayer
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.location.AndroidUserLocationProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppPreferences> { AndroidAppPreferences(androidContext()) }
    single<AudioGuidePlayer> { AndroidAudioGuidePlayer(androidContext()) }
    single<UserLocationProvider> { AndroidUserLocationProvider(androidContext()) }
}
