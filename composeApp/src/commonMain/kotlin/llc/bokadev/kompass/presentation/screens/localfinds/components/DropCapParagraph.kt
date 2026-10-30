package llc.bokadev.kompass.presentation.screens.localfinds.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.max
import llc.bokadev.kompass.ui.theme.KompassTheme

/**
 * A paragraph opening with a large initial (stories spec §2.2): the first lines wrap beside the
 * letter, the rest run full width. The split is taken at a measured line end, so no word breaks
 * differently from a normal paragraph. Screen readers get the paragraph as plain text.
 * With [enabled] false (large font scale) it's an ordinary paragraph.
 */
@Composable
fun DropCapParagraph(
    text: String,
    style: TextStyle,
    color: Color,
    capStyle: TextStyle,
    capColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!enabled || text.length < 2 || !text.first().isLetter()) {
        Text(text, style = style, color = color, modifier = modifier)
        return
    }
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val cap = text.substring(0, 1)
    val rest = text.substring(1)
    BoxWithConstraints(modifier.clearAndSetSemantics { this.text = AnnotatedString(text) }) {
        val gap = with(density) { 10.dp.roundToPx() }
        val capLayout = measurer.measure(cap, capStyle)
        val lineHeight = with(density) { style.lineHeight.toPx() }
        val besideLines = max(1, ceil(capLayout.size.height / lineHeight).toInt())
        val narrow = (constraints.maxWidth - capLayout.size.width - gap).coerceAtLeast(1)
        val restLayout = measurer.measure(rest, style, constraints = Constraints(maxWidth = narrow))
        val splitLine = minOf(besideLines, restLayout.lineCount) - 1
        val splitAt = restLayout.getLineEnd(splitLine, visibleEnd = false)
        Column {
            Row {
                Text(cap, style = capStyle, color = capColor, modifier = Modifier.padding(end = 10.dp))
                Text(rest.substring(0, splitAt).trimEnd(), style = style, color = color)
            }
            rest.substring(splitAt).trimStart().takeIf { it.isNotEmpty() }?.let { Text(it, style = style, color = color) }
        }
    }
}

@Preview
@Composable
private fun DropCapParagraphPreview() {
    DropCapParagraph(
        text = "Long before the first stone was laid, a wise fairy named Alkima warned the founders of Kotor not to build on the heights.",
        style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 18.sp, lineHeight = 30.6.sp),
        color = KompassTheme.colors.boneWhite,
        capStyle = KompassTheme.typography.gloockRegular40.copy(fontSize = 64.sp, lineHeight = 64.sp),
        capColor = KompassTheme.colors.lampYellow,
        enabled = true
    )
}
