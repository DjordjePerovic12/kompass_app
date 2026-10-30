package llc.bokadev.kompass.presentation.screens.essentials.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.presentation.shared.icons.EssentialsGlyph
import llc.bokadev.kompass.presentation.shared.icons.EssentialsIcon
import llc.bokadev.kompass.presentation.shared.icons.glyph

/** A shortcut tile; [emergency] gives it the only red treatment on the screen. */
data class UtilityTile(val label: String, val glyph: EssentialsGlyph, val emergency: Boolean = false, val onClick: () -> Unit)

/**
 * Category shortcuts (spec §3.3): each tile opens the in-app map for that category. They are
 * shortcuts, not filters, so there's no selected state. [columns] drops at large font scales.
 */
@Composable
fun UtilityCategoryGrid(tiles: List<UtilityTile>, columns: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { tile -> Tile(tile, Modifier.weight(1f).fillMaxHeight()) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Tile(tile: UtilityTile, modifier: Modifier) {
    val colors = KompassTheme.colors
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .heightIn(min = 84.dp)
            .clip(shape)
            .background(if (tile.emergency) KompassTheme.colors.emberBrown else colors.charcoal)
            .then(if (tile.emergency) Modifier.border(1.dp, KompassTheme.colors.rustBrown, shape) else Modifier)
            .clickable(onClick = tile.onClick)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(if (tile.emergency) colors.coralRed else colors.slateDark),
            contentAlignment = Alignment.Center
        ) {
            EssentialsIcon(tile.glyph, color = if (tile.emergency) colors.nightBlack else colors.lampYellow, size = 20.dp)
        }
        Text(
            text = tile.label,
            style = KompassTheme.typography.geologicaRegular13.copy(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold),
            color = colors.boneWhite,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun UtilityCategoryGridPreview() {
    UtilityCategoryGrid(
        tiles = listOf(
            UtilityTile("Cash & ATMs", EssentialsGlyph.Cash) {},
            UtilityTile("Pharmacy", EssentialsGlyph.Cross) {},
            UtilityTile("Parking", EssentialsGlyph.Parking) {},
            UtilityTile("All on map", EssentialsGlyph.Map) {}
        ),
        columns = 4
    )
}
