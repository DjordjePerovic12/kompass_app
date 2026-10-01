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
actual fun NativeDetailMapView(
    placeName: String,
    destination: GeoPoint,
    currentLocation: GeoPoint?,
    modifier: Modifier
) {
    if (BuildKonfig.GOOGLE_MAPS_API_KEY.isBlank()) {
        InlineHtmlMapView(
            html = buildGuideMapHtml(placeName, destination, currentLocation),
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember {
        MapsInitializer.initialize(context)
        MapView(context).apply {
            onCreate(Bundle())
        }
    }
    val mapContainer = remember(mapView) {
        GestureAwareMapContainer(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            if (mapView.parent != null) {
                (mapView.parent as? ViewGroup)?.removeView(mapView)
            }
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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                val destinationLatLng = LatLng(destination.latitude, destination.longitude)
                map.uiSettings.isZoomControlsEnabled = false
                map.uiSettings.isMapToolbarEnabled = false
                map.uiSettings.isCompassEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                map.clear()

                map.addMarker(
                    MarkerOptions()
                        .position(destinationLatLng)
                        .title(placeName)
                )

                currentLocation?.let { current ->
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(current.latitude, current.longitude))
                            .title("You")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                }

                if (currentLocation != null) {
                    val bounds = LatLngBounds.builder()
                        .include(destinationLatLng)
                        .include(LatLng(currentLocation.latitude, currentLocation.longitude))
                        .build()
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
                } else {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15f))
                }
            }
        }
    )
}

private class GestureAwareMapContainer(
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
