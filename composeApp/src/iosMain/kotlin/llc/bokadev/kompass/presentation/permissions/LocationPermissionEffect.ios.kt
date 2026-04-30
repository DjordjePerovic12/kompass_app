package llc.bokadev.kompass.presentation.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject

@Composable
actual fun LocationPermissionEffect(
    enabled: Boolean,
    onPermissionResult: (Boolean) -> Unit
) {
    val requester = remember { IosLocationPermissionRequester(onPermissionResult) }
    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose { }
        requester.requestPermissionIfNeeded()
        onDispose { requester.dispose() }
    }
}

private class IosLocationPermissionRequester(
    private val onPermissionResult: (Boolean) -> Unit
) : NSObject(), CLLocationManagerDelegateProtocol {
    private val manager = CLLocationManager()
    private var lastReported: Boolean? = null

    init {
        manager.delegate = this
    }

    fun requestPermissionIfNeeded() {
        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> notifyResult(true)
            kCLAuthorizationStatusNotDetermined -> manager.requestWhenInUseAuthorization()
            else -> notifyResult(false)
        }
    }

    fun dispose() {
        manager.delegate = null
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> notifyResult(true)
            kCLAuthorizationStatusNotDetermined -> Unit
            else -> notifyResult(false)
        }
    }

    private fun notifyResult(granted: Boolean) {
        if (lastReported == granted) return
        lastReported = granted
        onPermissionResult(granted)
    }
}
