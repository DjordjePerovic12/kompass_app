package llc.bokadev.kompass.presentation.screens.localfinds.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme

/** A localism, or a story without a photo: `limestone` card, a faint Gloock “, the phrase large. */
@Composable
fun StoryQuoteCard(
    label: String,
    title: String,
    excerpt: String?,
    readTime: String?,
    listen: String?,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.limestone)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { role = Role.Button; contentDescription = description }
    ) {
        Text(
            "“",
            style = KompassTheme.typography.gloockRegular40.copy(fontSize = 120.sp, lineHeight = 120.sp),
            color = colors.nightBlack.copy(alpha = 0.14f),
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 14.dp).offset(y = (-14).dp)
        )
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = KompassTheme.typography.geologicaSemiBold11, color = colors.inkGray)
            Text(
                title,
                style = KompassTheme.typography.gloockRegular34.copy(fontSize = 34.sp, lineHeight = 34.sp),
                color = colors.nightBlack,
                modifier = Modifier.padding(end = 40.dp)
            )
            excerpt?.let {
                Text(
                    it,
                    style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
                    color = colors.inkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StoryMetaRow(readTime, listen, colors.inkGray, colors.nightBlack, Modifier.padding(top = 2.dp))
        }
    }
}

@Preview
@Composable
private fun StoryQuoteCardPreview() {
    StoryQuoteCard("LOCALISM", "Polako", "Slowly, take it easy: what locals say when you rush.", "1 min read", null, "Localism, Polako", {})
}
