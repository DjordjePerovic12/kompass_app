package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import llc.bokadev.kompass.domain.model.GeoPoint
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKPointAnnotation
import platform.darwin.NSObject

/**
 * Keeps the annotations for one map: pins are rebuilt only when the pin set changes, the "you"
 * annotation is moved in place, and a tap reports the pin's id (never its title).
 */
@OptIn(ExperimentalForeignApi::class)
private class UtilityMapDelegate : NSObject(), MKMapViewDelegateProtocol {
    var onPinSelected: (String) -> Unit = {}
    var pinIds: List<String> = emptyList()
    val annotationsById = mutableMapOf<String, MKPointAnnotation>()
    var userAnnotation: MKPointAnnotation? = null
    val camera = UtilityMapCameraTracker()

    override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
        val annotation = didSelectAnnotationView.annotation ?: return
        annotationsById.entries.firstOrNull { it.value === annotation }?.let { onPinSelected(it.key) }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeUtilityDetailMapView(
    pins: List<UtilityMapPin>,
    currentLocation: GeoPoint?,
    currentLocationLabel: String,
    focusedPinId: String?,
    onPinSelected: (id: String) -> Unit,
    modifier: Modifier,
    recenterRequest: Int,
    // Not applied on iOS yet: MKMapView needs setVisibleMapRect(edgePadding:) for this.
    obscuredTop: Dp,
    obscuredBottom: Dp
) {
    if (pins.isEmpty()) return

    val currentOnPinSelected by rememberUpdatedState(onPinSelected)
    // MKMapView holds its delegate weakly; remembering it keeps it alive with the composable.
    val delegate = remember { UtilityMapDelegate() }
    delegate.onPinSelected = { currentOnPinSelected(it) }

    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                scrollEnabled = true
                zoomEnabled = true
                pitchEnabled = false
                rotateEnabled = false
                showsCompass = false
                setDelegate(delegate)
            }
        },
        update = { mapView ->
            val ids = pins.map { it.id }
            if (ids != delegate.pinIds) {
                delegate.annotationsById.values.forEach { mapView.removeAnnotation(it) }
                delegate.annotationsById.clear()
                pins.forEach { pin ->
                    val annotation = MKPointAnnotation().apply {
                        setCoordinate(CLLocationCoordinate2DMake(pin.point.latitude, pin.point.longitude))
                        setTitle(pin.title)
                    }
                    delegate.annotationsById[pin.id] = annotation
                    mapView.addAnnotation(annotation)
                }
                delegate.pinIds = ids
            }

            if (currentLocation == null) {
                delegate.userAnnotation?.let { mapView.removeAnnotation(it) }
                delegate.userAnnotation = null
            } else {
                val coordinate = CLLocationCoordinate2DMake(currentLocation.latitude, currentLocation.longitude)
                val user = delegate.userAnnotation ?: MKPointAnnotation().also {
                    delegate.userAnnotation = it
                    it.setCoordinate(coordinate)
                    mapView.addAnnotation(it)
                }
                user.setCoordinate(coordinate)
                user.setTitle(currentLocationLabel)
            }

            delegate.annotationsById[focusedPinId]?.let { focused ->
                val alreadySelected = mapView.selectedAnnotations.any { it === focused }
                if (!alreadySelected) mapView.selectAnnotation(focused, animated = false)
            }

            when (val move = delegate.camera.next(pins, focusedPinId, currentLocation, recenterRequest)) {
                is UtilityMapCameraMove.Frame -> mapView.setRegion(regionFitting(move.points), animated = false)
                is UtilityMapCameraMove.FocusOn -> mapView.setCenterCoordinate(
                    CLLocationCoordinate2DMake(move.point.latitude, move.point.longitude),
                    animated = true
                )
                UtilityMapCameraMove.None -> Unit
            }
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun regionFitting(points: List<GeoPoint>) = if (points.size == 1) {
    MKCoordinateRegionMakeWithDistance(
        CLLocationCoordinate2DMake(points.first().latitude, points.first().longitude),
        1400.0,
        1400.0
    )
} else {
    val minLat = points.minOf { it.latitude }
    val maxLat = points.maxOf { it.latitude }
    val minLng = points.minOf { it.longitude }
    val maxLng = points.maxOf { it.longitude }
    MKCoordinateRegionMakeWithDistance(
        CLLocationCoordinate2DMake((minLat + maxLat) / 2.0, (minLng + maxLng) / 2.0),
        ((maxLat - minLat) * 111_000.0 + 1200.0).coerceAtLeast(1200.0),
        ((maxLng - minLng) * 111_000.0 + 1200.0).coerceAtLeast(1200.0)
    )
}
