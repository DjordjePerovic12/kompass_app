package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.core.utils.RedesignStrings
import llc.bokadev.kompass.core.utils.redesignStringsFor
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes

/**
 * "Lamplight" Must See card (Must See spec Part A). [areaLabel] arrives already uppercased and
 * localized ("PERAST", "WORTH THE DRIVE") or null when the area is unknown, in which case the
 * line is hidden. [description] is the CMS short description only; blank hides the line.
 *
 * Fills the height its row gives it ([fillMaxHeight]) and pins the save button to the bottom
 * with a weight spacer, so buttons line up across cards of different text lengths. Width is the
 * caller's (196dp, or 224dp at large font scales). Sized down from the spec's 232dp / 196dp image
 * at the owner's request (2026-10-06); titles ellipsize after 3 lines.
 */
@Composable
fun MustSeeCard(
    name: String,
    areaLabel: String?,
    description: String?,
    imageUrl: String?,
    isSaved: Boolean,
    strings: RedesignStrings,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val haptics = LocalHapticFeedback.current
    val hasUrl = !imageUrl.isNullOrBlank()
    var loadFailed by remember(imageUrl) { mutableStateOf(false) }
    val shape = KompassShapes.cardMustSee
    val toggleSave = {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onToggleSave()
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .background(colors.limestone)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = listOfNotNull(name, areaLabel, description?.takeIf { it.isNotBlank() })
                    .joinToString(". ")
                role = Role.Button
                customActions = listOf(
                    CustomAccessibilityAction(if (isSaved) strings.removeFromSavedAction else strings.saveAction) { toggleSave(); true }
                )
            }
    ) {
        if (hasUrl && !loadFailed) {
            // limestoneShade shows while the photo loads; a failure swaps to the short block below.
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { loadFailed = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(shape)
                    .background(colors.limestoneDark)
            )
        } else {
            // A missing image must not become a large block of empty colour: 72dp, not the full 140dp.
            Box(
                modifier = Modifier.fillMaxWidth().height(72.dp).clip(shape).background(colors.limestoneDark),
                contentAlignment = Alignment.Center
            ) {
                LandmarkIcon(color = colors.mossGray, size = 24.dp)
            }
        }

        // The body keeps its natural height; the flexible space goes in the spacer below, so the
        // card measures correctly when its row probes it with unbounded height.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!areaLabel.isNullOrBlank()) {
                Text(
                    text = areaLabel,
                    style = KompassTheme.typography.geologicaSemiBold11,
                    color = colors.ashGray,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = name,
                style = KompassTheme.typography.gloockRegular23.copy(fontSize = 20.sp, lineHeight = 22.sp),
                color = colors.nightBlack,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = KompassTheme.typography.geologicaRegular14,
                    color = colors.inkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
                .fillMaxWidth()
                .height(48.dp)
                .clip(KompassShapes.pill)
                .then(
                    if (isSaved) Modifier.background(colors.nightBlack)
                    else Modifier.border(1.5.dp, colors.nightBlack, KompassShapes.pill)
                )
                .clickable(onClick = toggleSave)
                .semantics {
                    role = Role.Button
                    contentDescription = (if (isSaved) strings.removeTitleFromSaved else strings.saveTitle)
                        .replace("{title}", name)
                    stateDescription = if (isSaved) strings.mustSeeSaved else strings.notSaved
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val contentColor = if (isSaved) colors.chalkWhite else colors.nightBlack
            Text(
                text = if (isSaved) strings.mustSeeSaved else strings.mustSeeSave,
                style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = contentColor
            )
            if (isSaved) BookmarkIcon(filled = true, color = contentColor, size = 16.dp)
            else PlusIcon(color = contentColor, size = 16.dp)
        }
    }
}

@Preview
@Composable
private fun MustSeeCardPreview() {
    Row(Modifier.height(360.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MustSeeCard(
            name = "Our Lady of the Rocks",
            areaLabel = "PERAST",
            description = "An islet built stone by stone by local sailors.",
            imageUrl = null,
            isSaved = false,
            strings = redesignStringsFor("en"),
            onToggleSave = {},
            onClick = {},
            modifier = Modifier.width(196.dp)
        )
        MustSeeCard(
            name = "Lovćen Mausoleum",
            areaLabel = "WORTH THE DRIVE",
            description = null,
            imageUrl = null,
            isSaved = true,
            strings = redesignStringsFor("en"),
            onToggleSave = {},
            onClick = {},
            modifier = Modifier.width(196.dp)
        )
    }
}
