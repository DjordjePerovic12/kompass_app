package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import llc.bokadev.kompass.domain.model.GeoPoint
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeDetailMapView(
    placeName: String,
    destination: GeoPoint,
    currentLocation: GeoPoint?,
    modifier: Modifier
) {
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

            val destinationCoordinate =
                CLLocationCoordinate2DMake(destination.latitude, destination.longitude)
            val destinationAnnotation = MKPointAnnotation().apply {
                setCoordinate(destinationCoordinate)
                setTitle(placeName)
            }
            mapView.addAnnotation(destinationAnnotation)

            currentLocation?.let { current ->
                val currentAnnotation = MKPointAnnotation().apply {
                    setCoordinate(CLLocationCoordinate2DMake(current.latitude, current.longitude))
                    setTitle("You")
                }
                mapView.addAnnotation(currentAnnotation)

                val centerLat = (destination.latitude + current.latitude) / 2.0
                val centerLon = (destination.longitude + current.longitude) / 2.0
                val latMeters = kotlin.math.abs(destination.latitude - current.latitude) * 111_000 + 900
                val lonMeters = kotlin.math.abs(destination.longitude - current.longitude) * 111_000 + 900
                mapView.setRegion(
                    MKCoordinateRegionMakeWithDistance(
                        CLLocationCoordinate2DMake(centerLat, centerLon),
                        latMeters.coerceAtLeast(1200.0),
                        lonMeters.coerceAtLeast(1200.0)
                    ),
                    animated = false
                )
            } ?: run {
                mapView.setRegion(
                    MKCoordinateRegionMakeWithDistance(
                        destinationCoordinate,
                        1400.0,
                        1400.0
                    ),
                    animated = false
                )
            }
        }
    )
}
