package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint

@Composable
fun rememberLiveCurrentLocation(
    locationProvider: UserLocationProvider,
    refreshIntervalMillis: Long = 5_000L,
    vararg keys: Any?
): GeoPoint? {
    var currentLocation by remember(*keys) { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(locationProvider, refreshIntervalMillis, *keys) {
        if (!locationProvider.hasPermission()) {
            currentLocation = null
            return@LaunchedEffect
        }

        while (true) {
            currentLocation = runCatching { locationProvider.getCurrentLocation() }.getOrNull()
            delay(refreshIntervalMillis)
        }
    }

    return currentLocation
}
