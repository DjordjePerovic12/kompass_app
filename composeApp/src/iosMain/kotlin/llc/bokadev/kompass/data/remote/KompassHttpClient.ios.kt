package llc.bokadev.kompass.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.request.header
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.util.AppPreferences

actual fun createKompassHttpClient(preferences: AppPreferences): HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }
        )
    }

    install(DefaultRequest) {
        val language = preferences.getSelectedLanguage()
        header("Accept-Language", language)
        header("X-App-Language", language)
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) = println("Payment API: $message")
        }
        level = LogLevel.INFO
    }
}
