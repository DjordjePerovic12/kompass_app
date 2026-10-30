package llc.bokadev.kompass.core.utils.networking

/**
  True when the exception means the device could not reach the server
  (no connection, DNS failure). Each engine throws its own type, so this is platform specific.
 **/

expect fun Throwable.isNoInternet(): Boolean
