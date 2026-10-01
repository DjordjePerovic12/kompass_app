package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import llc.bokadev.kompass.domain.model.GeoPoint

data class UtilityMapPin(
    val title: String,
    val point: GeoPoint
)

@Composable
expect fun NativeUtilityPreviewMapView(
    pins: List<UtilityMapPin>,
    currentLocation: GeoPoint?,
    modifier: Modifier = Modifier
)
