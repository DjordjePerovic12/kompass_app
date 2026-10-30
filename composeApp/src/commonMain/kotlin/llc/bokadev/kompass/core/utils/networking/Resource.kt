package llc.bokadev.kompass.core.utils.networking

/**
    This approach to a custom API result handling was taken from PL tutorials.
    We liked this approach because it makes the API calls in repo,
    and also in enables us to handle API errors in a more efficient manner.
 **/

/**
    out keyword is for covariant types that are used in generics
    covariant means that the type can only be returned and not consumed
 **/

/**
    In Resource
    out D represents data - our APIs response
    out E represents Error - our custom interface we use to represent the NetworkError enum
    out M represents the custom error message we might get from the backend
    We will use both Error adn message in case the backed returns no message
    or a non formatted HTML response
 **/

typealias DomainError = Error

sealed interface Resource<out D, out E : Error, out M : String?> {
    data class Success<out D>(val data: D) : Resource<D, Nothing, Nothing>
    data class Error<out E : DomainError, out M : String?>(val error: E, val message: String?) :
        Resource<Nothing, E, M>
}
