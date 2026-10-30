package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.distanceKmTo
import llc.bokadev.kompass.presentation.shared.NativeDetailMapView
import llc.bokadev.kompass.ui.theme.KompassTheme

/**
 * "Where it is": a pannable map with the user's dot for quick orientation (owner's call, over
 * the spec's static preview), the area, and Open in Maps for directions and travel times.
 */
@Composable
fun DetailWhereItIs(
    title: String,
    name: String,
    destination: GeoPoint,
    currentLocation: GeoPoint?,
    areaLabel: String?,
    openInMapsLabel: String,
    onOpenMaps: () -> Unit,
    modifier: Modifier = Modifier
) {
    // (0, 0) is how a missing coordinate arrives from the CMS; never pin the Gulf of Guinea.
    if (destination.latitude == 0.0 && destination.longitude == 0.0) return
    val colors = KompassTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = title, style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite, modifier = Modifier.semantics { heading() })
        Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp))) {
            // The map frames the place and the user together, so a user far away (another country,
            // an emulator's default location) would zoom it out to a continent: show them only nearby.
            val nearbyUser = currentLocation?.takeIf { it.distanceKmTo(destination) <= NEARBY_USER_KM }
            NativeDetailMapView(placeName = name, destination = destination, currentLocation = nearbyUser, modifier = Modifier.fillMaxSize())
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = areaLabel.orEmpty(), style = KompassTheme.typography.geologicaRegular14, color = colors.stoneGray, modifier = Modifier.weight(1f))
            DetailPillButton(label = openInMapsLabel, glyph = DetailGlyph.ArrowUpRight, filled = false, height = 48.dp, onClick = onOpenMaps)
        }
    }
}

private const val NEARBY_USER_KM = 30.0

@Preview
@Composable
private fun DetailWhereItIsPreview() {
    DetailWhereItIs("Where it is", "Kotor", GeoPoint(42.4247, 18.7712), null, "Old Town", "Open in Maps", {})
}
