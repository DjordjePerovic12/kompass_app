package llc.bokadev.kompass.presentation.shared

import android.os.Bundle
import android.view.ViewGroup
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
actual fun NativeUtilityPreviewMapView(
    pins: List<UtilityMapPin>,
    currentLocation: GeoPoint?,
    modifier: Modifier
) {
    if (pins.isEmpty()) return

    if (BuildKonfig.GOOGLE_MAPS_API_KEY.isBlank()) {
        InlineHtmlMapView(
            html = buildGuideMapHtml(
                placeName = pins.first().title,
                destination = pins.first().point,
                currentLocation = currentLocation
            ),
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
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
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
        factory = { mapView },
        update = { view ->
            view.getMapAsync { map ->
                map.clear()
                map.uiSettings.apply {
                    isZoomControlsEnabled = false
                    isMapToolbarEnabled = false
                    isCompassEnabled = false
                    isMyLocationButtonEnabled = false
                    isScrollGesturesEnabled = false
                    isTiltGesturesEnabled = false
                    isRotateGesturesEnabled = false
                    isZoomGesturesEnabled = false
                }

                val bounds = LatLngBounds.builder()

                pins.forEach { pin ->
                    val latLng = LatLng(pin.point.latitude, pin.point.longitude)
                    map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(pin.title)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
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

                runCatching {
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 120))
                }.getOrElse {
                    val first = pins.first().point
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(first.latitude, first.longitude),
                            14.5f
                        )
                    )
                }
            }
        }
    )
}
