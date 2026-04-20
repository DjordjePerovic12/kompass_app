package llc.bokadev.kompass.di

import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.core.util.IosAppPreferences
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppPreferences> { IosAppPreferences() }
}
