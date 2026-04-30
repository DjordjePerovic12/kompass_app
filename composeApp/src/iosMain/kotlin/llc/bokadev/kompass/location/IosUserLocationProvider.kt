package llc.bokadev.kompass.location

import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IosUserLocationProvider : UserLocationProvider {
    private val manager = CLLocationManager()
    private val delegate = Delegate()

    init {
        manager.delegate = delegate
    }

    override fun hasPermission(): Boolean {
        return when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> true
            else -> false
        }
    }

    override suspend fun getCurrentLocation(): GeoPoint? {
        if (!hasPermission()) return null

        manager.location?.let { location ->
            return location.toGeoPoint()
        }

        return suspendCancellableCoroutine { cont ->
            delegate.onLocationResolved = { location ->
                cont.resume(location?.toGeoPoint())
            }
            manager.requestLocation()
            cont.invokeOnCancellation {
                delegate.onLocationResolved = null
            }
        }
    }

    private class Delegate : NSObject(), CLLocationManagerDelegateProtocol {
        var onLocationResolved: ((CLLocation?) -> Unit)? = null

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation
            onLocationResolved?.invoke(location)
            onLocationResolved = null
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            onLocationResolved?.invoke(null)
            onLocationResolved = null
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CLLocation.toGeoPoint(): GeoPoint {
    return GeoPoint(
        latitude = coordinate.useContents { latitude },
        longitude = coordinate.useContents { longitude }
    )
}
