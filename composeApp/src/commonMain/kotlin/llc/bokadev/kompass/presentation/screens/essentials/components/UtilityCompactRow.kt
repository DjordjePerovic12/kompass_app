package llc.bokadev.kompass.presentation.screens.essentials.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/** A secondary utility category (spec §3.5): icon, name, "{n} nearby", closest distance, chevron. */
@Composable
fun UtilityCompactRow(
    title: String,
    subtitle: String,
    glyph: EssentialsGlyph,
    closestDistance: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LamplightTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surfaceRaised)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.surfaceMuted), contentAlignment = Alignment.Center) {
            EssentialsIcon(glyph, color = colors.lamp, size = 20.dp)
        }
        Column(Modifier.weight(1f)) {
            Text(text = title, style = LamplightType.bodyL(), color = colors.textPrimary)
            Text(text = subtitle, style = LamplightType.meta(), color = colors.textTertiary)
        }
        closestDistance?.let {
            Text(text = it, style = LamplightType.body().copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = colors.lamp)
        }
        EssentialsIcon(EssentialsGlyph.ChevronDown, color = colors.textTertiary, size = 20.dp, modifier = Modifier.graphicsLayer { rotationZ = -90f })
    }
}

@Preview
@Composable
private fun UtilityCompactRowPreview() {
    UtilityCompactRow("Parking", "12 nearby", EssentialsGlyph.Parking, "240 m", {})
}
