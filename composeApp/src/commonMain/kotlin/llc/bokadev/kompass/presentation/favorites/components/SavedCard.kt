package llc.bokadev.kompass.presentation.favorites.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.shared.icons.BookmarkIcon
import llc.bokadev.kompass.presentation.shared.icons.PlayIcon
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.icons.DetailIcon
import llc.bokadev.kompass.ui.theme.KompassTheme

/** The remove button's warm dark fill behind the lamp bookmark (Saved spec §3). */

/**
 * One saved place or activity (Saved spec §3): photo, type label, Gloock title, and a meta line
 * (Audio · distance · one practical hint). The whole card opens the detail; the bookmark button
 * removes it and never opens the detail. [distance] is null outside the bay.
 */
@Composable
fun SavedCard(
    title: String,
    typeLabel: String?,
    imageUrl: String?,
    hasAudio: Boolean,
    audioLabel: String,
    distance: String?,
    hint: String?,
    accessibilityDescription: String,
    removeDescription: String,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    var imageFailed by remember(imageUrl) { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.charcoal)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpen)
                .clearAndSetSemantics {
                    role = Role.Button
                    contentDescription = accessibilityDescription
                    onClick { onOpen(); true }
                },
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(Modifier.size(104.dp).clip(RoundedCornerShape(16.dp)).background(colors.slateDark), contentAlignment = Alignment.Center) {
                if (!imageUrl.isNullOrBlank() && !imageFailed) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onError = { imageFailed = true },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    DetailIcon(DetailGlyph.Landmark, color = colors.pebbleGray, size = 28.dp)
                }
            }
            Column(Modifier.weight(1f).padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                typeLabel?.let {
                    Text(text = it, style = KompassTheme.typography.geologicaSemiBold11.copy(fontSize = 10.sp), color = colors.lampYellow, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    text = title,
                    style = KompassTheme.typography.gloockRegular23.copy(fontSize = 21.sp, lineHeight = 23.sp),
                    color = colors.boneWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasAudio) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PlayIcon(color = colors.boneWhite, size = 10.dp)
                            Text(text = audioLabel, style = KompassTheme.typography.geologicaRegular13.copy(fontWeight = FontWeight.SemiBold), color = colors.boneWhite)
                        }
                    }
                    listOfNotNull(distance, hint).forEach {
                        Text(text = it, style = KompassTheme.typography.geologicaRegular13, color = colors.pebbleGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(KompassTheme.colors.lampShadow)
                .clickable(onClick = onRemove)
                .semantics { role = Role.Button; contentDescription = removeDescription },
            contentAlignment = Alignment.Center
        ) {
            BookmarkIcon(filled = true, color = colors.lampYellow, size = 16.dp)
        }
    }
}

@Preview
@Composable
private fun SavedCardPreview() {
    SavedCard(
        title = "Our Lady of the Rocks", typeLabel = "CHURCH · ISLET", imageUrl = null, hasAudio = true,
        audioLabel = "Audio", distance = null, hint = "About 30 min",
        accessibilityDescription = "Our Lady of the Rocks", removeDescription = "Remove Our Lady of the Rocks from saved",
        onOpen = {}, onRemove = {}
    )
}
