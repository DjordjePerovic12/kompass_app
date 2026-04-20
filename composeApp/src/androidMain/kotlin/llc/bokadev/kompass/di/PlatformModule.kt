package llc.bokadev.kompass.di

import llc.bokadev.kompass.core.util.AndroidAppPreferences
import llc.bokadev.kompass.core.util.AppPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppPreferences> { AndroidAppPreferences(androidContext()) }
}
