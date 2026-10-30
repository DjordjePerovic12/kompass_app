package llc.bokadev.kompass.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark

@OptIn(ExperimentalForeignApi::class)
actual fun openUtilityCategoryInMaps(
    categoryQuery: String,
    centerLat: Double,
    centerLng: Double,
    currentLat: Double?,
    currentLng: Double?,
    fallbackLat: Double?,
    fallbackLng: Double?,
    pins: List<UtilityMapPin>
): Boolean {
    if (pins.isEmpty()) return false

    return runCatching {
        val items = pins.map { pin ->
            val placemark = MKPlacemark(
                coordinate = CLLocationCoordinate2DMake(
                    pin.point.latitude,
                    pin.point.longitude
                )
            )
            MKMapItem(placemark).apply {
                name = pin.title
            }
        }

        MKMapItem.openMapsWithItems(
            mapItems = items,
            launchOptions = null
        )
        true
    }.getOrDefault(false)
}
