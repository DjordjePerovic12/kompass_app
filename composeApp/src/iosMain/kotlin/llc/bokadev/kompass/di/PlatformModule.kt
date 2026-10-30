package llc.bokadev.kompass.di

import llc.bokadev.kompass.core.utils.AudioGuidePlayer
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.utils.IosConnectivityMonitor
import llc.bokadev.kompass.core.utils.IosAudioGuidePlayer
import llc.bokadev.kompass.core.utils.IosAppPreferences
import llc.bokadev.kompass.core.utils.IosOfflineAudioStore
import llc.bokadev.kompass.core.utils.OfflineAudioStore
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.location.IosUserLocationProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppPreferences> { IosAppPreferences() }
    single<AudioGuidePlayer> { IosAudioGuidePlayer() }
    single<OfflineAudioStore> { IosOfflineAudioStore() }
    single<ConnectivityMonitor> { IosConnectivityMonitor() }
    single<UserLocationProvider> { IosUserLocationProvider() }
}
