package llc.bokadev.kompass

import android.app.Application
import llc.bokadev.kompass.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class KompassApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@KompassApplication)
            modules(appModule)
        }
    }
}
