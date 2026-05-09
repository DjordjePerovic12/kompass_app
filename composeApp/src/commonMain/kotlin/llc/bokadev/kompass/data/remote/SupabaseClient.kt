package llc.bokadev.kompass.data.remote

import io.github.aakira.napier.Napier
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.request.header
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import llc.bokadev.kompass.BuildKonfig
import llc.bokadev.kompass.core.util.AppPreferences

@OptIn(SupabaseInternal::class)
fun createKompassSupabaseClient(
    preferences: AppPreferences
) = createSupabaseClient(
    supabaseUrl = if (BuildKonfig.USE_LOCAL_SUPABASE)
        "http://${BuildKonfig.SUPABASE_LOCAL_HOST}:54321"
    else
        BuildKonfig.SUPABASE_URL,
    supabaseKey = if (BuildKonfig.USE_LOCAL_SUPABASE)
        BuildKonfig.SUPABASE_ANON_KEY_LOCAL
    else
        BuildKonfig.SUPABASE_ANON_KEY
) {
    install(Postgrest)
    install(Auth)
    install(Storage)

    httpConfig {
        install(DefaultRequest) {
            val language = preferences.getSelectedLanguage()
            header("Accept-Language", language)
            header("X-App-Language", language)
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.d(message, tag = "KompassAPI")
                }
            }
            level = LogLevel.ALL
        }
    }
}
