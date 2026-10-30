package llc.bokadev.kompass.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import llc.bokadev.kompass.core.utils.StringProvider
import llc.bokadev.kompass.core.utils.StringProviderImpl

/**
 * Module dedicated to string resource management and localization.
 * Keeps string-related dependencies isolated and maintainable.
 */

val stringProviderModule = module {
    singleOf(::StringProviderImpl) bind StringProvider::class
}
