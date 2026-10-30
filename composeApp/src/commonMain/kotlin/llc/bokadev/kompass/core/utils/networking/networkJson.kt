package llc.bokadev.kompass.core.utils.networking

import kotlinx.serialization.json.Json

/**
  The one Json configuration used by the HttpClient and by parseErrorMessage.
  ignoreUnknownKeys keeps DTOs small: map only the fields the app uses.
 **/

val networkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}
