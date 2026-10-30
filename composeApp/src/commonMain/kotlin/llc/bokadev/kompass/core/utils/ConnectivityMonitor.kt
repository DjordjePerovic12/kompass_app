package llc.bokadev.kompass.core.utils

import kotlinx.coroutines.flow.StateFlow

/** Whether the device currently has a usable network. Starts optimistic (online) until told otherwise. */
interface ConnectivityMonitor {
    val isOnline: StateFlow<Boolean>
}
