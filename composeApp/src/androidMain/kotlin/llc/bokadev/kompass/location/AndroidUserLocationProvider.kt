package llc.bokadev.kompass.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint

class AndroidUserLocationProvider(
    private val context: Context
) : UserLocationProvider {

    override fun hasPermission(): Boolean = hasLocationPermission()

    override suspend fun getCurrentLocation(): GeoPoint? {
        if (!hasLocationPermission()) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val providers = runCatching { locationManager.getProviders(true) }
            .getOrDefault(emptyList())

        val bestLocation = providers
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .minByOrNull { it.accuracy }

        return bestLocation?.let { location ->
            GeoPoint(
                latitude = location.latitude,
                longitude = location.longitude
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasFine || hasCoarse
    }
}
