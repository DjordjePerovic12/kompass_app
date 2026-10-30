package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import llc.bokadev.kompass.domain.model.GeoPoint

/** One utility marker. [id] is the utility's stable id and the only lookup key; [title] is display-only. */
data class UtilityMapPin(
    val id: String,
    val title: String,
    val point: GeoPoint
)

@Composable
expect fun NativeUtilityPreviewMapView(
    pins: List<UtilityMapPin>,
    currentLocation: GeoPoint?,
    modifier: Modifier = Modifier
)
