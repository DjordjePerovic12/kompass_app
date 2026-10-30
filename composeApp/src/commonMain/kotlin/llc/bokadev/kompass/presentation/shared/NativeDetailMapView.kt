package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import llc.bokadev.kompass.domain.model.GeoPoint

@Composable
expect fun NativeDetailMapView(
    placeName: String,
    destination: GeoPoint,
    currentLocation: GeoPoint?,
    modifier: Modifier = Modifier,
    /** False: a static preview that ignores gestures and never steals the page's scroll. */
    interactive: Boolean = true
)
