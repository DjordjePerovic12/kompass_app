package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import llc.bokadev.kompass.domain.model.GeoPoint
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeUtilityDetailMapView(
    pins: List<UtilityMapPin>,
    currentLocation: GeoPoint?,
    focusedPinTitle: String?,
    onPinSelected: (String) -> Unit,
    modifier: Modifier
) {
    if (pins.isEmpty()) return

    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                scrollEnabled = true
                zoomEnabled = true
                pitchEnabled = false
                rotateEnabled = false
                showsCompass = false
            }
        },
        update = { mapView ->
            mapView.removeAnnotations(mapView.annotations)

            val allPoints = mutableListOf<GeoPoint>()
            pins.forEach { pin ->
                allPoints += pin.point
                val annotation = MKPointAnnotation().apply {
                    setCoordinate(CLLocationCoordinate2DMake(pin.point.latitude, pin.point.longitude))
                    setTitle(pin.title)
                }
                mapView.addAnnotation(annotation)
            }

            currentLocation?.let { user ->
                allPoints += user
                val userAnnotation = MKPointAnnotation().apply {
                    setCoordinate(CLLocationCoordinate2DMake(user.latitude, user.longitude))
                    setTitle("You")
                }
                mapView.addAnnotation(userAnnotation)
            }

            val focusedPin = pins.firstOrNull { it.title == focusedPinTitle } ?: pins.first()
            if (pins.size == 1) {
                mapView.setRegion(
                    MKCoordinateRegionMakeWithDistance(
                        CLLocationCoordinate2DMake(focusedPin.point.latitude, focusedPin.point.longitude),
                        1400.0,
                        1400.0
                    ),
                    animated = false
                )
            } else {
                val latitudes = allPoints.map { it.latitude }
                val longitudes = allPoints.map { it.longitude }
                val centerLat = (latitudes.minOrNull()!! + latitudes.maxOrNull()!!) / 2.0
                val centerLng = (longitudes.minOrNull()!! + longitudes.maxOrNull()!!) / 2.0
                val latMeters = ((latitudes.maxOrNull()!! - latitudes.minOrNull()!!) * 111_000.0 + 1200.0)
                    .coerceAtLeast(1200.0)
                val lngMeters = ((longitudes.maxOrNull()!! - longitudes.minOrNull()!!) * 111_000.0 + 1200.0)
                    .coerceAtLeast(1200.0)

                mapView.setRegion(
                    MKCoordinateRegionMakeWithDistance(
                        CLLocationCoordinate2DMake(centerLat, centerLng),
                        latMeters,
                        lngMeters
                    ),
                    animated = false
                )
            }
        }
    )
}
