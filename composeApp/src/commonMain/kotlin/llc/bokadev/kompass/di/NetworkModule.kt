package llc.bokadev.kompass.di

import llc.bokadev.kompass.data.remote.createKompassSupabaseClient
import org.koin.dsl.module

val networkModule = module {
    single { createKompassSupabaseClient() }
}
