package llc.bokadev.kompass.di

import llc.bokadev.kompass.core.util.AudioGuidePlayer
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.core.util.IosAudioGuidePlayer
import llc.bokadev.kompass.core.util.IosAppPreferences
import llc.bokadev.kompass.core.util.IosOfflineAudioStore
import llc.bokadev.kompass.core.util.OfflineAudioStore
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.location.IosUserLocationProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppPreferences> { IosAppPreferences() }
    single<AudioGuidePlayer> { IosAudioGuidePlayer() }
    single<OfflineAudioStore> { IosOfflineAudioStore() }
    single<UserLocationProvider> { IosUserLocationProvider() }
}
