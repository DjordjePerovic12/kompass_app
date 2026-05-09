package llc.bokadev.kompass.data.remote

import io.ktor.client.HttpClient
import llc.bokadev.kompass.core.util.AppPreferences

expect fun createKompassHttpClient(preferences: AppPreferences): HttpClient
