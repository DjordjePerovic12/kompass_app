package llc.bokadev.kompass

import android.app.Application
import llc.bokadev.kompass.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class KompassApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@KompassApplication)
        }
    }
}
