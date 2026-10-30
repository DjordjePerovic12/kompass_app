package llc.bokadev.kompass.presentation.screens.favorites.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes

/**
 * A saved id whose content was unpublished or deleted (Saved spec §4.2): dashed outline, the
 * cached name, and a Remove button. Deliberately not tappable, so it never opens an empty detail.
 */
@Composable
fun UnavailableSavedCard(
    title: String,
    subtitle: String,
    removeLabel: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = colors.slateGray,
                    cornerRadius = CornerRadius(22.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                )
            }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = colors.stoneGray)
            Text(text = subtitle, style = KompassTheme.typography.geologicaRegular13, color = colors.pebbleGray)
        }
        Text(
            text = removeLabel,
            style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
            color = colors.boneWhite,
            modifier = Modifier
                .heightIn(min = 44.dp)
                .clip(KompassShapes.pill)
                .border(1.dp, colors.pebbleGray, KompassShapes.pill)
                .clickable(onClick = onRemove)
                .semantics { role = Role.Button }
                .padding(horizontal = 18.dp, vertical = 12.dp)
        )
    }
}

@Preview
@Composable
private fun UnavailableSavedCardPreview() {
    UnavailableSavedCard("Church of Saint Luke", "No longer available in KOMPASS", "Remove", {})
}
