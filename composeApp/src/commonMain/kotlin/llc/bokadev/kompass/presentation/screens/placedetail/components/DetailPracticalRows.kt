package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.uppercaseFor
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

data class DetailPracticalRow(val glyph: DetailGlyph, val label: String, val value: String, val secondary: String? = null)

/**
 * The detail pages' practical facts between dividers: [fullWidth] rows one per line, then up to
 * two [paired] rows side by side (stacked at large text). Renders nothing when both are empty.
 */
@Composable
fun DetailPracticalRows(
    fullWidth: List<DetailPracticalRow>,
    paired: List<DetailPracticalRow>,
    lang: String,
    modifier: Modifier = Modifier
) {
    if (fullWidth.isEmpty() && paired.isEmpty()) return
    val colors = LamplightTheme.colors
    val stackPair = LocalDensity.current.fontScale >= 1.3f
    Column(modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp)) {
        RowDivider()
        fullWidth.forEach { row ->
            RowItem(row, lang, Modifier.fillMaxWidth())
            RowDivider()
        }
        if (paired.size == 2 && !stackPair) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                RowItem(paired[0], lang, Modifier.weight(1f))
                Box(Modifier.width(1.dp).fillMaxHeight().background(colors.divider))
                RowItem(paired[1], lang, Modifier.weight(1f).padding(start = 12.dp))
            }
            RowDivider()
        } else {
            paired.forEach { row ->
                RowItem(row, lang, Modifier.fillMaxWidth())
                RowDivider()
            }
        }
    }
}

@Composable
private fun RowDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(LamplightTheme.colors.divider))
}

@Composable
private fun RowItem(row: DetailPracticalRow, lang: String, modifier: Modifier) {
    val colors = LamplightTheme.colors
    val spoken = listOfNotNull(row.value, row.secondary).joinToString(" · ")
    Row(modifier = modifier.padding(vertical = 14.dp).clearAndSetSemantics { contentDescription = "${row.label}: $spoken" }) {
        Box(Modifier.width(28.dp)) { DetailIcon(row.glyph, color = colors.lamp, size = 20.dp) }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = row.label.uppercaseFor(lang), style = LamplightType.label(), color = colors.textTertiary)
            Text(text = row.value, style = LamplightType.body(), color = colors.textPrimary)
            row.secondary?.let { Text(text = it, style = LamplightType.meta(), color = colors.textSecondary) }
        }
    }
}

@Preview
@Composable
private fun DetailPracticalRowsPreview() {
    DetailPracticalRows(
        fullWidth = emptyList(),
        paired = listOf(DetailPracticalRow(DetailGlyph.Visit, "Duration", "About 3 h"), DetailPracticalRow(DetailGlyph.Sun, "Best time", "Morning")),
        lang = "en"
    )
}
