package llc.bokadev.kompass.core.utils.networking

/**
  Enum class representing standardized network error types.
  Implements Error interface for consistent error handling across the application.

  Each error type corresponds to common network-related issues:
 **/
enum class NetworkError : Error {
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    REQUEST_TIMEOUT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    SERVER_ERROR,
    SERIALIZATION,
    NOT_FOUND,
    UNKNOWN
}
