package llc.bokadev.kompass.core.utils.networking

import io.ktor.util.network.UnresolvedAddressException
import java.net.ConnectException
import java.net.UnknownHostException

actual fun Throwable.isNoInternet(): Boolean =
    this is UnresolvedAddressException || this is UnknownHostException || this is ConnectException
