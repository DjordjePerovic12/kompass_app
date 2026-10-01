package llc.bokadev.kompass.presentation.shared

import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    focusedPinTitle: String?,
    onPinSelected: (String) -> Unit,
    modifier: Modifier
) {
    if (pins.isEmpty()) return

    if (BuildKonfig.GOOGLE_MAPS_API_KEY.isBlank()) {
        val selectedPin = pins.firstOrNull { it.title == focusedPinTitle } ?: pins.first()
        InlineHtmlMapView(
            html = buildGuideMapHtml(selectedPin.title, selectedPin.point, currentLocation),
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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

                val bounds = LatLngBounds.builder()
                pins.forEach { pin ->
                    val latLng = LatLng(pin.point.latitude, pin.point.longitude)
                    val hue = if (pin.title == focusedPinTitle) {
                        BitmapDescriptorFactory.HUE_RED
                    } else {
                        BitmapDescriptorFactory.HUE_ORANGE
                    }
                    map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(pin.title)
                            .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    )
                    bounds.include(latLng)
                }

                currentLocation?.let { user ->
                    val userLatLng = LatLng(user.latitude, user.longitude)
                    map.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("You")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                    bounds.include(userLatLng)
                }

                map.setOnMarkerClickListener { marker ->
                    marker.title?.let(onPinSelected)
                    false
                }

                val focusedPin = pins.firstOrNull { it.title == focusedPinTitle } ?: pins.first()
                if (pins.size == 1) {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(focusedPin.point.latitude, focusedPin.point.longitude),
                            15f
                        )
                    )
                } else {
                    runCatching {
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 140))
                    }.getOrElse {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(focusedPin.point.latitude, focusedPin.point.longitude),
                                13.6f
                            )
                        )
                    }
                }
            }
        }
    )
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
