package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes

/** The detail pages' pill action: filled `lamp` (primary) or a `lamp` outline (secondary). */
@Composable
fun DetailPillButton(
    label: String,
    glyph: DetailGlyph?,
    filled: Boolean,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val content = if (filled) colors.nightBlack else colors.lampYellow
    Row(
        modifier = modifier
            .height(height)
            .clip(KompassShapes.pill)
            .then(if (filled) Modifier.background(colors.lampYellow) else Modifier.border(1.5.dp, colors.lampYellow, KompassShapes.pill))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        glyph?.let { DetailIcon(it, color = content, size = 16.dp) }
        Text(
            text = label,
            style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun DetailPillButtonPreview() {
    DetailPillButton("Open in Maps", DetailGlyph.ArrowUpRight, filled = false, height = 48.dp, onClick = {})
}
