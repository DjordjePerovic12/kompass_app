package llc.bokadev.kompass.presentation.localfinds.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.shared.icons.HeadphonesIcon
import llc.bokadev.kompass.presentation.search.components.SearchGlyph
import llc.bokadev.kompass.presentation.search.components.SearchGlyphIcon
import llc.bokadev.kompass.ui.theme.KompassTheme

/** 📖 read time · 🎧 Listen (in [listenColor]). Either part hides when null. Decorative for TalkBack (the card describes itself). */
@Composable
fun StoryMetaRow(
    readTime: String?,
    listen: String?,
    color: Color,
    listenColor: Color,
    modifier: Modifier = Modifier
) {
    if (readTime == null && listen == null) return
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        readTime?.let {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                SearchGlyphIcon(SearchGlyph.Book, color, 14.dp)
                Text(it, style = KompassTheme.typography.geologicaRegular13, color = color)
            }
        }
        listen?.let {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                HeadphonesIcon(listenColor, 13.dp)
                Text(it, style = KompassTheme.typography.geologicaRegular13, color = listenColor)
            }
        }
    }
}

@Preview
@Composable
private fun StoryMetaRowPreview() {
    KompassTheme {
        StoryMetaRow("4 min read", "Listen", KompassTheme.colors.stoneGray, KompassTheme.colors.lampYellow)
    }
}
