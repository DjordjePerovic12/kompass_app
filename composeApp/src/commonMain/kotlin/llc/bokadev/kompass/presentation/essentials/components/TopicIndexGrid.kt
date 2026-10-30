package llc.bokadev.kompass.presentation.essentials.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.presentation.shared.icons.EssentialsGlyph
import llc.bokadev.kompass.presentation.shared.icons.EssentialsIcon
import llc.bokadev.kompass.presentation.shared.icons.glyph

data class TopicTile(val title: String, val glyph: EssentialsGlyph, val onClick: () -> Unit)

/**
 * "What do you need?" (spec §2.4): 2 columns of jump tiles (1 column when [singleColumn]). An odd
 * last tile spans the full width so the grid never leaves a hole.
 */
@Composable
fun TopicIndexGrid(tiles: List<TopicTile>, singleColumn: Boolean, modifier: Modifier = Modifier) {
    val rows = if (singleColumn) tiles.map { listOf(it) } else tiles.chunked(2)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { tile -> Tile(tile, Modifier.weight(1f).fillMaxHeight()) }
            }
        }
    }
}

@Composable
private fun Tile(tile: TopicTile, modifier: Modifier) {
    val colors = KompassTheme.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clip(shape)
            .background(colors.charcoal)
            .border(1.dp, colors.slateDivider, shape)
            .clickable(onClick = tile.onClick)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        EssentialsIcon(tile.glyph, color = colors.lampYellow, size = 24.dp)
        Text(text = tile.title, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = colors.boneWhite)
    }
}

@Preview
@Composable
private fun TopicIndexGridPreview() {
    KompassTheme {
        TopicIndexGrid(
            tiles = listOf(
                TopicTile("Registration & tax", EssentialsGlyph.Document) {},
                TopicTile("Getting around", EssentialsGlyph.Bus) {},
                TopicTile("Practical info", EssentialsGlyph.Wallet) {}
            ),
            singleColumn = false
        )
    }
}
