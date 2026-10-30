package llc.bokadev.kompass.core.utils.networking

import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
    This is the function we will use to make API calls. Renaming can be considered.
    Pass the ApiService call that returns HttpResponse, e.g.
    retrieveAPIResponse<TestResponseDto> { apiService.getDataResponseTest() }
 **/

suspend inline fun <reified T> retrieveAPIResponse(
    execute: () -> HttpResponse
): Resource<T, NetworkError, String> {
    val response = try {
        execute()
    } catch (e: HttpRequestTimeoutException) {
        return Resource.Error(NetworkError.REQUEST_TIMEOUT, "Request timed out")
    } catch (e: SocketTimeoutException) {
        return Resource.Error(NetworkError.REQUEST_TIMEOUT, "Request timed out")
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        if (e.isNoInternet()) {
            return Resource.Error(NetworkError.NO_INTERNET, "Check your internet connection")
        }
        return Resource.Error(NetworkError.UNKNOWN, "Unknown error")
    }

    return responseToResource(response)
}
