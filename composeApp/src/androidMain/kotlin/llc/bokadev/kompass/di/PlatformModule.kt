package llc.bokadev.kompass.di

import llc.bokadev.kompass.core.utils.AndroidAppPreferences
import llc.bokadev.kompass.core.utils.AndroidAudioGuidePlayer
import llc.bokadev.kompass.core.utils.AndroidOfflineAudioStore
import llc.bokadev.kompass.core.utils.AudioGuidePlayer
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.utils.AndroidConnectivityMonitor
import llc.bokadev.kompass.core.utils.OfflineAudioStore
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.location.AndroidUserLocationProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppPreferences> { AndroidAppPreferences(androidContext()) }
    single<AudioGuidePlayer> { AndroidAudioGuidePlayer(androidContext()) }
    single<OfflineAudioStore> { AndroidOfflineAudioStore(androidContext()) }
    single<ConnectivityMonitor> { AndroidConnectivityMonitor(androidContext()) }
    single<UserLocationProvider> { AndroidUserLocationProvider(androidContext()) }
}
