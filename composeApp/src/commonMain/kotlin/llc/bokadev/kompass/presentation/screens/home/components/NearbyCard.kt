package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.utils.RedesignStrings
import llc.bokadev.kompass.core.utils.redesignStringsFor
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.presentation.shared.icons.ArrowRightIcon
import llc.bokadev.kompass.presentation.shared.icons.BookmarkIcon
import llc.bokadev.kompass.presentation.shared.icons.HeadphonesIcon

/** Card geometry (cut-out spec B2, 390dp reference). Mirrored by `CardSpec` in `tools/cutout/make_cutout.py`. */
private val cardHeight = 410.dp
private val titleInset = 22.dp
private val titleWidth = 196.dp // safe zone x 22 → 218
private val slotHeight = 250.dp
private val buttonMargin = 18.dp
private val buttonSize = 56.dp

/**
 * The "Lamplight" Nearby front card: a `lamp` panel with the title top-left, the place photo in an
 * arch in the bottom-right slot ([CutoutSlot]), and a button row overlapping the slot: a chalk
 * pill (Listen when the place has audio, else Open) plus a round save button.
 *
 * Slot rule (spec B8 rule 7): a title of 4+ lines or a font scale of 1.3+ narrows the slot to
 * 50% width; if the title block reaches into the slot's top edge, the image is dropped entirely.
 * Without a photo the card is compact (text-first, full width) instead of a large empty panel.
 */
@Composable
fun NearbyCard(
    title: String,
    meta: String,
    imageUrl: String?,
    hasAudio: Boolean,
    isSaved: Boolean,
    strings: RedesignStrings,
    onClick: () -> Unit,
    onPrimaryClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    var titleLines by remember(title) { mutableIntStateOf(1) }
    var textBlockHeightPx by remember(title, meta) { mutableIntStateOf(0) }
    val textBottom = titleInset + with(density) { textBlockHeightPx.toDp() }
    val slotTop = cardHeight - slotHeight
    val slotFraction = if (titleLines >= 4 || density.fontScale >= 1.3f) 0.5f else 0.58f
    val photoFree = imageUrl.isNullOrBlank()
    val slotMode = when {
        imageUrl.isNullOrBlank() || textBottom > slotTop -> SlotMode.None
        else -> SlotMode.Arch(imageUrl)
    }
    val toggleSave = {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onToggleSave()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (photoFree) Modifier else Modifier.height(cardHeight))
            .clip(KompassShapes.cardL)
            .background(colors.lampYellow)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = listOf(title, meta).filter { it.isNotBlank() }.joinToString(". ")
                role = Role.Button
                customActions = listOf(
                    CustomAccessibilityAction(if (isSaved) strings.removeFromSavedAction else strings.saveAction) { toggleSave(); true }
                )
            }
    ) {
        if (!photoFree) CutoutSlot(
            mode = slotMode,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(slotFraction)
                .height(slotHeight)
        )

        Column(
            modifier = Modifier
                .padding(start = titleInset, top = titleInset, end = if (photoFree) titleInset else 0.dp)
                // Photo-free: leave room for the button row below instead of a fixed-height panel.
                .padding(bottom = if (photoFree) buttonSize + buttonMargin * 2 else 0.dp)
                .then(if (photoFree) Modifier.fillMaxWidth() else Modifier.width(titleWidth))
                .onSizeChanged { textBlockHeightPx = it.height },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = KompassTheme.typography.gloockRegular34,
                color = colors.nightBlack,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { titleLines = it.lineCount }
            )
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = KompassTheme.typography.geologicaRegular13,
                    color = colors.inkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(buttonMargin),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(buttonSize)
                    .clip(KompassShapes.pill)
                    .background(colors.chalkWhite)
                    .clickable(onClick = onPrimaryClick)
                    .semantics { role = Role.Button },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasAudio) HeadphonesIcon(color = colors.nightBlack, size = 18.dp)
                else ArrowRightIcon(color = colors.nightBlack, size = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (hasAudio) strings.listen else strings.open,
                    style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = colors.nightBlack
                )
            }
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(colors.chalkWhite)
                    .clickable(onClick = toggleSave)
                    .semantics {
                        role = Role.Button
                        contentDescription = (if (isSaved) strings.removeTitleFromSaved else strings.saveTitle).replace("{title}", title)
                        stateDescription = if (isSaved) strings.mustSeeSaved else strings.notSaved
                    },
                contentAlignment = Alignment.Center
            ) {
                BookmarkIcon(filled = isSaved, color = colors.nightBlack, size = 18.dp)
            }
        }
    }
}

@Preview
@Composable
private fun NearbyCardPreview() {
    NearbyCard(
        title = "Cathedral of Saint Tryphon",
        meta = "Cathedral · 240 m",
        imageUrl = null,
        hasAudio = true,
        isSaved = false,
        strings = redesignStringsFor("en"),
        onClick = {},
        onPrimaryClick = {},
        onToggleSave = {},
        modifier = Modifier.width(338.dp)
    )
}
