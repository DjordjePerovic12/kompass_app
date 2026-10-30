package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.presentation.screens.home.components.GlobeIcon
import llc.bokadev.kompass.presentation.screens.home.components.SearchIcon
import llc.bokadev.kompass.presentation.theme.LamplightDimens
import llc.bokadev.kompass.presentation.theme.LamplightShapes
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/**
 * The yellow Lamplight header shared by Home, Explore and Essentials: wordmark + language button,
 * a screen-specific [headline] (Home's area picker, a screen title), the destination search field
 * (omitted when [onSearchClick] is null), and an optional [footer] (Home's status line, the
 * Essentials view switch). The wall-line decoration is purely decorative.
 */
@Composable
fun LamplightHeader(
    searchPlaceholder: String?,
    onLanguageClick: () -> Unit,
    onSearchClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
    headline: @Composable ColumnScope.() -> Unit
) {
    val colors = LamplightTheme.colors
    val languageCode = currentAppLanguage().uppercase()

    Box(modifier = modifier.fillMaxWidth().background(colors.lamp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .align(Alignment.TopEnd)
        ) {
            drawZigzag(colors.night)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = LamplightDimens.screenPadding)
                .padding(top = 4.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KOMPASS",
                    style = LamplightType.label().copy(letterSpacing = 0.3.em, fontWeight = FontWeight.Bold),
                    color = colors.night
                )
                Row(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clip(LamplightShapes.pill)
                        .background(colors.night.copy(alpha = 0.1f))
                        .semantics { contentDescription = "Language: $languageCode" }
                        .clickable(onClick = onLanguageClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlobeIcon(color = colors.night, size = 16.dp)
                    Text(text = languageCode, style = LamplightType.body(), color = colors.night)
                }
            }

            headline()

            if (searchPlaceholder != null && onSearchClick != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(LamplightShapes.searchField)
                        .background(colors.night)
                        .semantics { contentDescription = searchPlaceholder }
                        .clickable(onClick = onSearchClick)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SearchIcon(color = colors.textTertiary, size = 18.dp)
                    Text(text = searchPlaceholder, style = LamplightType.body(), color = colors.textTertiary)
                }
            }

            footer()
        }
    }
}

private fun DrawScope.drawZigzag(color: Color) {
    val w = size.width
    val h = size.height
    val path = Path()
    val segments = 7
    val startX = w * 0.42f
    val segW = (w - startX) / segments
    var x = startX
    var y = h * 0.08f
    path.moveTo(x, y)
    repeat(segments) { index ->
        x += segW
        y += if (index % 2 == 0) h * 0.1f else -h * 0.1f
        path.lineTo(x, y)
    }
    drawPath(path, color.copy(alpha = 0.22f), style = Stroke(width = 2.5.dp.toPx()))
}

@Preview
@Composable
private fun LamplightHeaderPreview() {
    LamplightHeader(searchPlaceholder = "Search Kotor and the bay", onLanguageClick = {}, onSearchClick = {}) {
        Text("Explore", style = LamplightType.areaTitle(), color = LamplightTheme.colors.night)
    }
}
