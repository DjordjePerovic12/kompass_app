package llc.bokadev.kompass.data.remote

import io.ktor.client.HttpClient

expect fun createKompassHttpClient(): HttpClient
