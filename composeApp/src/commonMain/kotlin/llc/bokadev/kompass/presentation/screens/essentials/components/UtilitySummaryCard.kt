package llc.bokadev.kompass.presentation.screens.essentials.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.presentation.shared.NativeUtilityPreviewMapView
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.presentation.shared.icons.EssentialsGlyph
import llc.bokadev.kompass.presentation.shared.icons.EssentialsIcon

/** One numbered "nearest" row. [distance] is null when location is off (never fake "closest"). */
data class NearestRow(val id: String, val name: String, val distance: String?)

/**
 * A top utility category (spec §3.4): title, "{n} nearby", a big Closest distance, a locked map
 * preview framing the nearest pins and the user, the numbered nearest list, and Open map.
 * [primary] makes Open map the screen's one filled action; later cards use the outline style.
 */
@Composable
fun UtilitySummaryCard(
    title: String,
    nearbyLabel: String,
    closestLabel: String,
    closestDistance: String?,
    pins: List<UtilityMapPin>,
    userLocation: GeoPoint?,
    mapDescription: String,
    nearest: List<NearestRow>,
    openMapLabel: String,
    primary: Boolean,
    onOpenMap: () -> Unit,
    onOpenItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.charcoal).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(text = title, style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite, modifier = Modifier.semantics { heading() })
                Text(text = nearbyLabel, style = KompassTheme.typography.geologicaRegular14, color = colors.stoneGray)
            }
            closestDistance?.let {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = closestLabel.uppercase(), style = KompassTheme.typography.geologicaSemiBold11.copy(fontSize = 10.sp), color = colors.pebbleGray)
                    Text(text = it, style = KompassTheme.typography.gloockRegular32.copy(fontSize = 30.sp, lineHeight = 32.sp), color = colors.lampYellow)
                }
            }
        }
        if (pins.isNotEmpty()) {
            Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp))) {
                NativeUtilityPreviewMapView(pins = pins, currentLocation = userLocation, modifier = Modifier.fillMaxSize())
                // The preview never pans or steals scroll; this layer makes a tap open the full map.
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable(onClick = onOpenMap)
                        .semantics { contentDescription = mapDescription; role = Role.Button }
                )
            }
        }
        Column {
            nearest.forEachIndexed { index, row ->
                if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.slateDivider))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onOpenItem(row.id) }
                        .semantics(mergeDescendants = true) { role = Role.Button },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(22.dp).clip(CircleShape).background(if (index == 0) colors.lampYellow else colors.slateDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = KompassTheme.typography.geologicaRegular13.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = if (index == 0) colors.nightBlack else colors.stoneGray
                        )
                    }
                    Text(
                        text = row.name,
                        style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = colors.boneWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    row.distance?.let {
                        Text(
                            text = it,
                            style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                            color = if (index == 0) colors.lampYellow else colors.stoneGray
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(KompassShapes.pill)
                .then(if (primary) Modifier.background(colors.lampYellow) else Modifier.border(1.5.dp, colors.lampYellow, KompassShapes.pill))
                .clickable(onClick = onOpenMap)
                .semantics(mergeDescendants = true) { role = Role.Button },
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val content = if (primary) colors.nightBlack else colors.lampYellow
            EssentialsIcon(EssentialsGlyph.Map, color = content, size = 16.dp)
            Text(text = openMapLabel, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = content)
        }
    }
}

@Preview
@Composable
private fun UtilitySummaryCardPreview() {
    UtilitySummaryCard(
        title = "Cash & ATMs", nearbyLabel = "11 nearby", closestLabel = "Closest", closestDistance = "86 m",
        pins = emptyList(), userLocation = null, mapDescription = "", openMapLabel = "Open map", primary = true,
        nearest = listOf(NearestRow("1", "NLB Banka", "86 m"), NearestRow("2", "Prva Banka", "140 m")),
        onOpenMap = {}, onOpenItem = {}
    )
}
