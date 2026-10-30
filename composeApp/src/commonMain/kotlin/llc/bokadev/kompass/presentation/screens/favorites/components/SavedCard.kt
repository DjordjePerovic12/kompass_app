package llc.bokadev.kompass.presentation.screens.favorites.components

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
import llc.bokadev.kompass.presentation.screens.home.components.BookmarkIcon
import llc.bokadev.kompass.presentation.screens.home.components.PlayIcon
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailGlyph
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailIcon
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/** The remove button's warm dark fill behind the lamp bookmark (Saved spec §3). */
private val RemoveFill = Color(0xFF2A2516)

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
    val colors = LamplightTheme.colors
    var imageFailed by remember(imageUrl) { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surfaceRaised)
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
            Box(Modifier.size(104.dp).clip(RoundedCornerShape(16.dp)).background(colors.surfaceMuted), contentAlignment = Alignment.Center) {
                if (!imageUrl.isNullOrBlank() && !imageFailed) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onError = { imageFailed = true },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    DetailIcon(DetailGlyph.Landmark, color = colors.textTertiary, size = 28.dp)
                }
            }
            Column(Modifier.weight(1f).padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                typeLabel?.let {
                    Text(text = it, style = LamplightType.label().copy(fontSize = 10.sp), color = colors.lamp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    text = title,
                    style = LamplightType.cardTitle().copy(fontSize = 21.sp, lineHeight = 23.sp),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasAudio) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PlayIcon(color = colors.textPrimary, size = 10.dp)
                            Text(text = audioLabel, style = LamplightType.meta().copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                        }
                    }
                    listOfNotNull(distance, hint).forEach {
                        Text(text = it, style = LamplightType.meta(), color = colors.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(RemoveFill)
                .clickable(onClick = onRemove)
                .semantics { role = Role.Button; contentDescription = removeDescription },
            contentAlignment = Alignment.Center
        ) {
            BookmarkIcon(filled = true, color = colors.lamp, size = 16.dp)
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
