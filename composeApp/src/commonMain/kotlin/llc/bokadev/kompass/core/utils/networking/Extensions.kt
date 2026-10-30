package llc.bokadev.kompass.core.utils.networking

import kotlin.coroutines.cancellation.CancellationException

/**
    Networking extensions
    map -> In case the API request is successful we need to map the response (DTO) to UI ready object (model)
    onSuccess() and onError() are used so the VMs are a bit tidier (avoiding the when block to check the response
    in VM)
 **/

inline fun <T, E: Error, M: String?, R> Resource<T, E, M>.map(map: (T) -> R): Resource<R, E, M> {
    return when(this) {
        is Resource.Error -> Resource.Error(error, message)
        is Resource.Success -> Resource.Success(map(data))
    }
}

inline fun <T, E: Error, M: String?> Resource<T, E, M>.onSuccess(action: (T) -> Unit): Resource<T, E, M> {
    return when(this) {
        is Resource.Error -> this
        is Resource.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E: Error> Resource<T, E, String?>.onError(action: (E, String?) -> Unit): Resource<T, E, String?> {
    return when(this) {
        is Resource.Error -> {
            action(error, message)
            this
        }
        is Resource.Success -> this
    }
}

inline fun <T, E: Error, M: String?> Resource<T, E, M>.getOrNull(): T? {
    return when(this) {
        is Resource.Error -> null
        is Resource.Success -> data
    }
}

inline fun <T, E: Error> Resource<T, E, String?>.recover(fallback: (E, String?) -> T?): Resource<T, E, String?> {
    return when(this) {
        is Resource.Error -> fallback(error, message)?.let { Resource.Success(it) } ?: this
        is Resource.Success -> this
    }
}

inline fun <T, R> Resource<T, NetworkError, String?>.mapCatching(map: (T) -> R): Resource<R, NetworkError, String?> {
    return when(this) {
        is Resource.Error -> Resource.Error(error, message)
        is Resource.Success -> try {
            Resource.Success(map(data))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(NetworkError.UNKNOWN, e.message)
        }
    }
}
