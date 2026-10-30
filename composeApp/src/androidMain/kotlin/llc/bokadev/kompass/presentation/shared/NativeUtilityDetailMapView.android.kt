package llc.bokadev.kompass.presentation.shared

import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import llc.bokadev.kompass.BuildKonfig
import llc.bokadev.kompass.core.util.buildGuideMapHtml
import llc.bokadev.kompass.domain.model.GeoPoint

@Composable
actual fun NativeUtilityDetailMapView(
    pins: List<UtilityMapPin>,
    currentLocation: GeoPoint?,
    currentLocationLabel: String,
    focusedPinId: String?,
    onPinSelected: (id: String) -> Unit,
    modifier: Modifier,
    recenterRequest: Int,
    obscuredTop: Dp,
    obscuredBottom: Dp
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val topPx = with(density) { obscuredTop.roundToPx() }
    val bottomPx = with(density) { obscuredBottom.roundToPx() }
    if (pins.isEmpty()) return

    if (BuildKonfig.GOOGLE_MAPS_API_KEY.isBlank()) {
        val selectedPin = pins.firstOrNull { it.id == focusedPinId } ?: pins.first()
        InlineHtmlMapView(
            html = buildGuideMapHtml(selectedPin.title, selectedPin.point, currentLocation),
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnPinSelected by rememberUpdatedState(onPinSelected)
    val cameraTracker = remember { UtilityMapCameraTracker() }
    val mapView = remember {
        MapsInitializer.initialize(context)
        MapView(context).apply { onCreate(Bundle()) }
    }
    val mapContainer = remember(mapView) {
        GestureAwareUtilityMapContainer(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(
                mapView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = mapView.onStart()
            override fun onResume(owner: LifecycleOwner) = mapView.onResume()
            override fun onPause(owner: LifecycleOwner) = mapView.onPause()
            override fun onStop(owner: LifecycleOwner) = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapContainer },
        update = { view ->
            if (mapView.parent !== view) {
                (mapView.parent as? ViewGroup)?.removeView(mapView)
                view.removeAllViews()
                view.addView(
                    mapView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }

            mapView.getMapAsync { map ->
                map.clear()
                // Overlays (title bar, selection sheet) shouldn't hide pins or the camera's centre.
                map.setPadding(0, topPx, 0, bottomPx)
                map.uiSettings.apply {
                    isZoomControlsEnabled = false
                    isMapToolbarEnabled = false
                    isCompassEnabled = false
                    isMyLocationButtonEnabled = false
                    isScrollGesturesEnabled = true
                    isTiltGesturesEnabled = false
                    isRotateGesturesEnabled = false
                    isZoomGesturesEnabled = true
                }

                pins.forEach { pin ->
                    val hue = if (pin.id == focusedPinId) {
                        BitmapDescriptorFactory.HUE_RED
                    } else {
                        BitmapDescriptorFactory.HUE_ORANGE
                    }
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(pin.point.latitude, pin.point.longitude))
                            .title(pin.title)
                            .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    )?.tag = pin.id
                }

                currentLocation?.let { user ->
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(user.latitude, user.longitude))
                            .title(currentLocationLabel)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                }

                // Only utility markers carry an id tag; the "you" marker is not selectable.
                map.setOnMarkerClickListener { marker ->
                    (marker.tag as? String)?.let { currentOnPinSelected(it) }
                    false
                }

                val move = cameraTracker.next(pins, focusedPinId, currentLocation, recenterRequest)
                // Bounds need the laid-out size; run after layout so the first frame fits the real map.
                view.post { applyCamera(map, move, pins, focusedPinId) }
            }
        }
    )
}

private fun applyCamera(
    map: com.google.android.gms.maps.GoogleMap,
    move: UtilityMapCameraMove,
    pins: List<UtilityMapPin>,
    focusedPinId: String?
) {
    when (move) {
        is UtilityMapCameraMove.Frame -> {
            val focusedPin = pins.firstOrNull { it.id == focusedPinId } ?: pins.first()
            val focusedLatLng = LatLng(focusedPin.point.latitude, focusedPin.point.longitude)
            if (move.points.size == 1) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(focusedLatLng, 15f))
            } else {
                val bounds = LatLngBounds.builder()
                move.points.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }
                runCatching {
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 96))
                }.getOrElse {
                    // If bounds still can't be applied, fall back to the focused pin.
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(focusedLatLng, 13.6f))
                }
            }
        }
        is UtilityMapCameraMove.FocusOn ->
            map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(move.point.latitude, move.point.longitude)))
        UtilityMapCameraMove.None -> Unit
    }
}

private class GestureAwareUtilityMapContainer(
    context: android.content.Context
) : FrameLayout(context) {

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_POINTER_UP -> parent?.requestDisallowInterceptTouchEvent(true)

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> parent?.requestDisallowInterceptTouchEvent(false)
        }
        return false
    }
}
