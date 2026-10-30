package llc.bokadev.kompass.core.utils.networking

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException

/**
    The Supabase SDK counterpart of retrieveAPIResponse: postgrest, storage and auth calls throw
    instead of returning an HttpResponse, so this maps their exceptions to a Resource.
    Cancellation is rethrown (runCatching would swallow it).

    safeSupabaseCall { supabase.from("events").select().decodeList<EventDto>() }
 **/

suspend inline fun <T> safeSupabaseCall(
    execute: () -> T
): Resource<T, NetworkError, String?> {
    return try {
        Resource.Success(execute())
    } catch (e: RestException) {
        val error = when (e.statusCode) {
            400 -> NetworkError.BAD_REQUEST
            401 -> NetworkError.UNAUTHORIZED
            403 -> NetworkError.FORBIDDEN
            404 -> NetworkError.NOT_FOUND
            408 -> NetworkError.REQUEST_TIMEOUT
            429 -> NetworkError.TOO_MANY_REQUESTS
            in 500..599 -> NetworkError.SERVER_ERROR
            else -> NetworkError.UNKNOWN
        }
        Resource.Error(error, e.description ?: e.error)
    } catch (e: HttpRequestTimeoutException) {
        Resource.Error(NetworkError.REQUEST_TIMEOUT, "Request timed out")
    } catch (e: SerializationException) {
        Resource.Error(NetworkError.SERIALIZATION, e.message)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        val noInternet = e.isNoInternet() || (e is HttpRequestException && e.cause?.isNoInternet() == true)
        if (noInternet) {
            Resource.Error(NetworkError.NO_INTERNET, "Check your internet connection")
        } else {
            Resource.Error(NetworkError.UNKNOWN, e.message)
        }
    }
}
