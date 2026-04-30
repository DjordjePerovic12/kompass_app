package llc.bokadev.kompass.domain.location

import llc.bokadev.kompass.domain.model.GeoPoint

interface UserLocationProvider {
    fun hasPermission(): Boolean
    suspend fun getCurrentLocation(): GeoPoint?
}
