package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import llc.bokadev.kompass.domain.model.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeUtilityPreviewMapView(
    pins: List<UtilityMapPin>,
    currentLocation: GeoPoint?,
    modifier: Modifier
) {
    if (pins.isEmpty()) return

    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                scrollEnabled = false
                zoomEnabled = false
                pitchEnabled = false
                rotateEnabled = false
                showsCompass = false
            }
        },
        update = { mapView ->
            mapView.removeAnnotations(mapView.annotations)

            val allPoints = mutableListOf<GeoPoint>()
            val clusterCenter = GeoPoint(
                latitude = pins.map { it.point.latitude }.average(),
                longitude = pins.map { it.point.longitude }.average()
            )
            val shouldShowCurrentLocation = currentLocation?.let { user ->
                pins.size <= 2 || haversineDistanceKm(user, clusterCenter) > 0.12
            } ?: false
            pins.forEach { pin ->
                allPoints += pin.point
                val annotation = MKPointAnnotation().apply {
                    setCoordinate(CLLocationCoordinate2DMake(pin.point.latitude, pin.point.longitude))
                    setTitle(pin.title)
                }
                mapView.addAnnotation(annotation)
            }

            if (shouldShowCurrentLocation) {
                val user = currentLocation!!
                allPoints += user
                val userAnnotation = MKPointAnnotation().apply {
                    setCoordinate(CLLocationCoordinate2DMake(user.latitude, user.longitude))
                    setTitle("You")
                }
                mapView.addAnnotation(userAnnotation)
            }

            val latitudes = allPoints.map { it.latitude }
            val longitudes = allPoints.map { it.longitude }
            val centerLat = (latitudes.minOrNull()!! + latitudes.maxOrNull()!!) / 2.0
            val centerLng = (longitudes.minOrNull()!! + longitudes.maxOrNull()!!) / 2.0
            val latMeters = ((latitudes.maxOrNull()!! - latitudes.minOrNull()!!) * 111_000.0 + 1000.0)
                .coerceAtLeast(900.0)
            val lngMeters = ((longitudes.maxOrNull()!! - longitudes.minOrNull()!!) * 111_000.0 + 1000.0)
                .coerceAtLeast(900.0)

            mapView.setRegion(
                MKCoordinateRegionMakeWithDistance(
                    CLLocationCoordinate2DMake(centerLat, centerLng),
                    latMeters,
                    lngMeters
                ),
                animated = false
            )
        }
    )
}

private fun haversineDistanceKm(from: GeoPoint, to: GeoPoint): Double {
    val earthRadiusKm = 6371.0
    val dLat = (to.latitude - from.latitude).toRadians()
    val dLon = (to.longitude - from.longitude).toRadians()
    val fromLat = from.latitude.toRadians()
    val toLat = to.latitude.toRadians()

    val a = sin(dLat / 2).pow(2) +
        cos(fromLat) * cos(toLat) * sin(dLon / 2).pow(2)
    val c = 2 * asin(sqrt(a))
    return earthRadiusKm * c
}

private fun Double.toRadians(): Double = this * (kotlin.math.PI / 180.0)
