package llc.bokadev.kompass.presentation.screens.localfinds.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.ui.theme.KompassTheme

/** The first story: 220dp photo, label, Gloock 30 title, 3-line excerpt, meta. One focus target. */
@Composable
fun StoryFeatureCard(
    label: String,
    title: String,
    excerpt: String?,
    photoUrl: String?,
    readTime: String?,
    listen: String?,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.charcoal)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { role = Role.Button; contentDescription = description }
    ) {
        if (photoUrl != null) {
            Box(Modifier.fillMaxWidth().height(220.dp).background(colors.slateDark)) {
                AsyncImage(model = photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(220.dp))
            }
        }
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(label, style = KompassTheme.typography.geologicaSemiBold11, color = colors.lampYellow)
            Text(title, style = KompassTheme.typography.gloockRegular34.copy(fontSize = 30.sp, lineHeight = 32.sp), color = colors.boneWhite)
            excerpt?.let {
                Text(
                    it,
                    style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 16.sp, lineHeight = 25.sp, fontWeight = FontWeight.Normal),
                    color = colors.stoneGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StoryMetaRow(readTime, listen, colors.stoneGray, colors.lampYellow)
        }
    }
}

@Preview
@Composable
private fun StoryFeatureCardPreview() {
    StoryFeatureCard(
        "LEGEND · OLD TOWN", "Alkima, the Fairy Who Built Kotor", "Long before the first stone was laid, a wise fairy named Alkima warned the founders.",
        null, "3 min read", "Listen", "Legend, Alkima", {}
    )
}
