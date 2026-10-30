package llc.bokadev.kompass.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Starts Koin. Android calls it from KompassApplication, iOS from iOSApp.swift (InitKoinKt.doInitKoin(config: nil)).
 */

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule)
    }
}
