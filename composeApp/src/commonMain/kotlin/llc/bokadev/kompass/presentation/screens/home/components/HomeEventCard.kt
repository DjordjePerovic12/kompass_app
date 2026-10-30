package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.presentation.shared.icons.ArrowRightIcon

/**
 * "Lamplight" event card (spec §3.5). Home-exclusive — not a restyle of the shared
 * `components/EventCard.kt`, which the standalone Events screen still uses with its own
 * defaults. No save/favorite affordance (event favorites were removed from pilot scope).
 * The title ellipsizes after 3 lines. Give it a fixed height (e.g. `fillMaxHeight()` inside Home's
 * equal-height row) and the time row stays at the bottom, so cards in a row match.
 */
@Composable
fun HomeEventCard(
    name: String,
    venueLine: String,
    dateTag: String,
    startTime: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val hasUrl = !imageUrl.isNullOrBlank()
    var loadFailed by remember(imageUrl) { mutableStateOf(false) }
    val cardShape = KompassShapes.cardEvent

    Column(
        modifier = modifier
            .clip(cardShape)
            .background(colors.charcoal)
            .border(1.dp, colors.slateDivider, cardShape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(124.dp)) {
            if (hasUrl && !loadFailed) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onError = { loadFailed = true },
                    modifier = Modifier.fillMaxSize().clip(KompassShapes.imageInner)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(KompassShapes.imageInner)
                        .background(colors.slateDark)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.lampYellow)
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
                Text(
                    text = dateTag,
                    style = KompassTheme.typography.geologicaSemiBold11.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = colors.nightBlack
                )
            }
        }

        Text(
            text = name,
            style = KompassTheme.typography.geologicaMedium16,
            color = colors.boneWhite,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        if (venueLine.isNotBlank()) {
            Text(
                text = venueLine,
                style = KompassTheme.typography.geologicaRegular13,
                color = colors.pebbleGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Pins the time row to the bottom when the row stretches this card to a taller sibling's height.
        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = startTime,
                style = KompassTheme.typography.geologicaSemiBold17,
                color = colors.boneWhite
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.slateDivider)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                ArrowRightIcon(color = colors.lampYellow, size = 16.dp)
            }
        }
    }
}
