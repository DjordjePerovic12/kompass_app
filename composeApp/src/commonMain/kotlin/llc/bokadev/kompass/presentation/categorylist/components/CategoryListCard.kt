package llc.bokadev.kompass.presentation.categorylist.components

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
import llc.bokadev.kompass.ui.theme.KompassTheme

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
    val colors = KompassTheme.colors
    val haptics = LocalHapticFeedback.current
    var imageFailed by remember(imageUrl) { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (saved) colors.charcoalLight else colors.charcoal)
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
            Box(Modifier.size(96.dp).clip(RoundedCornerShape(16.dp)).background(colors.slateDark), contentAlignment = Alignment.Center) {
                if (!imageUrl.isNullOrBlank() && !imageFailed) {
                    AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, onError = { imageFailed = true }, modifier = Modifier.fillMaxSize())
                } else {
                    DetailIcon(DetailGlyph.Landmark, color = colors.pebbleGray, size = 28.dp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                label?.let { Text(text = it, style = KompassTheme.typography.geologicaSemiBold11.copy(fontSize = 10.sp), color = colors.lampYellow, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                Text(
                    text = title,
                    style = KompassTheme.typography.gloockRegular23.copy(fontSize = 20.sp, lineHeight = 22.sp),
                    color = colors.boneWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (meta != null || hasAudio) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (hasAudio) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PlayIcon(color = colors.boneWhite, size = 10.dp)
                                Text(text = audioLabel, style = KompassTheme.typography.geologicaRegular13.copy(fontWeight = FontWeight.SemiBold), color = colors.boneWhite)
                            }
                        }
                        meta?.let { Text(text = it, style = KompassTheme.typography.geologicaRegular13, color = colors.pebbleGray, maxLines = 1) }
                    }
                }
            }
        }
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (saved) colors.lampShadow else colors.slateDark)
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
            BookmarkIcon(filled = saved, color = if (saved) colors.lampYellow else colors.stoneGray, size = 16.dp)
        }
    }
}

@Preview
@Composable
private fun CategoryListCardPreview() {
    KompassTheme {
        CategoryListCard(
            title = "Church of Saint Nicholas", label = "ORTHODOX CHURCH · OLD TOWN", imageUrl = null, meta = "1.2 km",
            hasAudio = false, audioLabel = "Audio", saved = true,
            saveDescription = "Remove Church of Saint Nicholas from saved", savedStateLabel = "Saved", onToggle = {}, onOpen = {}
        )
    }
}
