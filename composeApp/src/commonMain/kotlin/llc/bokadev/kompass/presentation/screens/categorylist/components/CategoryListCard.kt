package llc.bokadev.kompass.presentation.screens.categorylist.components

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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

private val SavedCardFill = Color(0xFF22262A)
private val SavedButtonFill = Color(0xFF2A2516)
private val CardShape = RoundedCornerShape(22.dp)

/**
 * A category-list row (category list spec §3). Saving happens in place: the bookmark and card fill
 * switch to the saved state with a light haptic tick; the row never moves, dims or glows.
 */
@Composable
fun CategoryListCard(
    title: String,
    label: String?,
    imageUrl: String?,
    meta: String?,
    hasAudio: Boolean,
    audioLabel: String,
    saved: Boolean,
    saveDescription: String,
    savedStateLabel: String,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LamplightTheme.colors
    val haptics = LocalHapticFeedback.current
    var imageFailed by remember(imageUrl) { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (saved) SavedCardFill else colors.surfaceRaised)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpen)
                .clearAndSetSemantics {
                    role = Role.Button
                    contentDescription = listOfNotNull(title, label, meta, audioLabel.takeIf { hasAudio }).joinToString(", ")
                    onClick { onOpen(); true }
                },
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(96.dp).clip(RoundedCornerShape(16.dp)).background(colors.surfaceMuted), contentAlignment = Alignment.Center) {
                if (!imageUrl.isNullOrBlank() && !imageFailed) {
                    AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, onError = { imageFailed = true }, modifier = Modifier.fillMaxSize())
                } else {
                    DetailIcon(DetailGlyph.Landmark, color = colors.textTertiary, size = 28.dp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                label?.let { Text(text = it, style = LamplightType.label().copy(fontSize = 10.sp), color = colors.lamp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                Text(
                    text = title,
                    style = LamplightType.cardTitle().copy(fontSize = 20.sp, lineHeight = 22.sp),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (meta != null || hasAudio) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (hasAudio) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PlayIcon(color = colors.textPrimary, size = 10.dp)
                                Text(text = audioLabel, style = LamplightType.meta().copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                            }
                        }
                        meta?.let { Text(text = it, style = LamplightType.meta(), color = colors.textTertiary, maxLines = 1) }
                    }
                }
            }
        }
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (saved) SavedButtonFill else colors.surfaceMuted)
                .clickable {
                    if (!saved) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle()
                }
                .semantics {
                    role = Role.Button
                    contentDescription = saveDescription
                    stateDescription = savedStateLabel
                },
            contentAlignment = Alignment.Center
        ) {
            BookmarkIcon(filled = saved, color = if (saved) colors.lamp else colors.textSecondary, size = 16.dp)
        }
    }
}

@Preview
@Composable
private fun CategoryListCardPreview() {
    CategoryListCard(
        title = "Church of Saint Nicholas", label = "ORTHODOX CHURCH · OLD TOWN", imageUrl = null, meta = "1.2 km",
        hasAudio = false, audioLabel = "Audio", saved = true,
        saveDescription = "Remove Church of Saint Nicholas from saved", savedStateLabel = "Saved", onToggle = {}, onOpen = {}
    )
}
