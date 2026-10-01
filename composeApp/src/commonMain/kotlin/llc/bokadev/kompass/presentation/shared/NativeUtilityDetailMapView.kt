package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import llc.bokadev.kompass.domain.model.GeoPoint

@Composable
expect fun NativeUtilityDetailMapView(
    pins: List<UtilityMapPin>,
    currentLocation: GeoPoint?,
    focusedPinTitle: String?,
    onPinSelected: (String) -> Unit,
    modifier: Modifier = Modifier
)
