package llc.bokadev.kompass

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.core.util.RevenueCatManager
import llc.bokadev.kompass.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class KompassApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
        val koinApp = startKoin {
            androidLogger()
            androidContext(this@KompassApplication)
            modules(appModule)
        }
        RevenueCatManager.configureForAndroid(koinApp.koin.get<AppPreferences>())
    }
}
