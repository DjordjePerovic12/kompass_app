package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.remote.createKompassHttpClient
import llc.bokadev.kompass.data.remote.createKompassSupabaseClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {
    single { createKompassSupabaseClient(get()) }
    single(named("kompassApi")) { createKompassHttpClient(get()) }
}
