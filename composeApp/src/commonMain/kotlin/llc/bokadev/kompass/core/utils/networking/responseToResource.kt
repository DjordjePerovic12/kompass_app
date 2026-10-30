package llc.bokadev.kompass.core.utils.networking

import co.touchlab.kermit.Logger
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

/**
This is the function we will use to turn the API response to our custom Resource
so we can handle it properly in the VMs.
 **/

/**
val errorBody -> bodyAsText() of a non 2xx response, the equivalent to response.body() in case of an error response
val errorMessage -> we need to map the information we get from our API error response
so it can visually be represented as it should. If we didn't do this,
instead of having a formatted error message, we would show the whole JSON of our
error response, instead of it's value
 **/

/**
reified keyword is used in inline functions to allow access to the actual type arguments at runtime
 **/

suspend inline fun <reified T> responseToResource(response: HttpResponse): Resource<T, NetworkError, String> {
    val code = response.status.value
    Logger.d { "RESPONSE CODE $code" }
    if (code in 200..299) {
        return try {
            Resource.Success(response.body<T>())
        } catch (e: Exception) {
            Resource.Error(NetworkError.SERIALIZATION, e.message)
        }
    }

    val errorBody = try {
        response.bodyAsText()
    } catch (e: Exception) {
        ""
    }
    val errorMessage = parseErrorMessage(errorBody)
    return when (code) {
        400 -> Resource.Error(NetworkError.BAD_REQUEST, errorMessage)
        401 -> Resource.Error(NetworkError.UNAUTHORIZED, errorMessage)
        403 -> Resource.Error(NetworkError.FORBIDDEN, errorMessage)
        404 -> Resource.Error(NetworkError.NOT_FOUND, errorMessage)
        408 -> Resource.Error(NetworkError.REQUEST_TIMEOUT, errorMessage)
        429 -> Resource.Error(NetworkError.TOO_MANY_REQUESTS, errorMessage)
        in 500..599 -> Resource.Error(NetworkError.SERVER_ERROR, errorMessage)
        else -> Resource.Error(NetworkError.UNKNOWN, errorMessage)
    }
}
