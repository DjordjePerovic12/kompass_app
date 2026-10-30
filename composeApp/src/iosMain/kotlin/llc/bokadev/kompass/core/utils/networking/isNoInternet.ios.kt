package llc.bokadev.kompass.core.utils.networking

import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.util.network.UnresolvedAddressException
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet

actual fun Throwable.isNoInternet(): Boolean = when (this) {
    is UnresolvedAddressException -> true
    is DarwinHttpRequestException -> origin.code in listOf(
        NSURLErrorNotConnectedToInternet,
        NSURLErrorCannotFindHost,
        NSURLErrorCannotConnectToHost,
        NSURLErrorNetworkConnectionLost
    )
    else -> false
}
