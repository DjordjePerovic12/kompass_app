package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.utils.buildMapsUrlForCoords
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.presentation.screens.home.components.ArrowRightIcon
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject

@Composable
fun DetailNativeMapCard(
    placeName: String,
    summary: String,
    destination: GeoPoint,
    modifier: Modifier = Modifier,
    titleColor: Color? = null,
    bodyColor: Color? = null,
    actionColor: Color? = null,
    borderColor: Color? = null
) {
    val colors = KompassTheme.colors
    val uriHandler = LocalUriHandler.current
    val locationProvider = koinInject<UserLocationProvider>()
    val currentLocation = rememberLiveCurrentLocation(
        locationProvider = locationProvider,
        refreshIntervalMillis = 5_000L,
        placeName,
        destination
    )
    val resolvedTitleColor = titleColor ?: colors.colorNavy
    val resolvedBodyColor = bodyColor ?: colors.colorSlate.copy(alpha = 0.78f)
    val resolvedActionColor = actionColor ?: colors.colorOrangeMain
    val resolvedBorderColor = borderColor ?: colors.colorSurfaceMid.copy(alpha = 0.78f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.colorWhite)
            .border(1.dp, resolvedBorderColor, RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        NativeDetailMapView(
            placeName = placeName,
            destination = destination,
            currentLocation = currentLocation,
            modifier = Modifier
                .fillMaxWidth()
                .height(214.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "Explore Around",
                style = MaterialTheme.typography.titleMedium.copy(lineHeight = 28.sp),
                color = resolvedTitleColor
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                color = resolvedBodyColor
            )
        }

        Row(
            modifier = Modifier.clickable {
                uriHandler.openUri(
                    buildMapsUrlForCoords(destination.latitude, destination.longitude)
                )
            },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Open in Maps app",
                style = MaterialTheme.typography.bodySmall,
                color = resolvedActionColor
            )
            ArrowRightIcon(color = resolvedActionColor, size = 12.dp)
        }
    }
}
