package llc.bokadev.kompass.core.util

import kotlinx.coroutines.flow.StateFlow

/** Whether the device currently has a usable network. Starts optimistic (online) until told otherwise. */
interface ConnectivityMonitor {
    val isOnline: StateFlow<Boolean>
}
