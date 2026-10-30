package llc.bokadev.kompass.presentation.screens.localfinds.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

/** A legend with a photo: 120dp photo column (min 160dp tall), label, Gloock 22 title, 2-line excerpt, meta. */
@Composable
fun StoryCompactCard(
    label: String,
    title: String,
    excerpt: String?,
    photoUrl: String,
    readTime: String?,
    listen: String?,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .heightIn(min = 160.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.charcoal)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { role = Role.Button; contentDescription = description }
    ) {
        Box(Modifier.width(120.dp).fillMaxHeight().background(colors.slateDark)) {
            AsyncImage(model = photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.width(120.dp).fillMaxHeight())
        }
        Column(Modifier.weight(1f).padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = KompassTheme.typography.geologicaSemiBold11.copy(fontSize = 10.sp), color = colors.lampYellow, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                title,
                style = KompassTheme.typography.gloockRegular23.copy(fontSize = 22.sp, lineHeight = 24.sp),
                color = colors.boneWhite,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            excerpt?.let {
                Text(
                    it,
                    style = KompassTheme.typography.geologicaRegular14.copy(lineHeight = 21.sp, fontWeight = FontWeight.Normal),
                    color = colors.stoneGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StoryMetaRow(readTime, listen, colors.stoneGray, colors.lampYellow, Modifier.padding(top = 2.dp))
        }
    }
}

@Preview
@Composable
private fun StoryCompactCardPreview() {
    StoryCompactCard("LEGEND · PERAST", "The stones of Our Lady of the Rocks", "Sailors added a stone each time they came home.", "", "2 min read", null, "Legend", {})
}
