package llc.bokadev.kompass.presentation.screens.category_items_list.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PriceIndicator
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun PlaceListItem(
    place: Place,
    lang: String = "en",
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Photo thumbnail
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.colorSlateGhost)
        ) {
            val imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) }
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = place.localizedName(lang),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
            // Price badge overlaid bottom-left
            place.priceIndicator?.let { price ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.colorNavy.copy(alpha = 0.75f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = price.dots,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.colorAmber
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = place.localizedName(lang),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val meta = listOfNotNull(
                place.zone,
                place.subCategory?.replaceFirstChar { it.uppercase() }
            ).joinToString(" · ")

            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.colorSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (place.estimatedDuration != null || place.tags.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    place.estimatedDuration?.let { minutes ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(10.dp)) { drawClock(colors.colorSlateLight) }
                            Text(
                                text = minutes.formatDuration(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.colorSlateLight
                            )
                        }
                    }
                    place.tags.firstOrNull()?.let { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.colorSlateFaint)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.colorSlate
                            )
                        }
                    }
                }
            }
        }

        // Chevron
        Canvas(modifier = Modifier.size(18.dp)) { drawChevron(colors.colorAmberDark) }
    }
}

private val PriceIndicator.dots: String
    get() = when (this) {
        PriceIndicator.BUDGET    -> "€"
        PriceIndicator.MODERATE  -> "€€"
        PriceIndicator.EXPENSIVE -> "€€€"
    }

private fun Int.formatDuration(): String = when {
    this < 60  -> "${this}min"
    this % 60 == 0 -> "${this / 60}h"
    else -> "${this / 60}h ${this % 60}min"
}

private fun DrawScope.drawChevron(color: Color) {
    val path = Path().apply {
        moveTo(size.width * 0.3f, size.height * 0.2f)
        lineTo(size.width * 0.7f, size.height * 0.5f)
        lineTo(size.width * 0.3f, size.height * 0.8f)
    }
    drawPath(path, color, style = Stroke(width = 1.8.dp.toPx()))
}

private fun DrawScope.drawClock(color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.width * 0.44f
    drawCircle(color = color, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.2.dp.toPx()))
    // hour hand
    drawLine(color = color, start = Offset(cx, cy), end = Offset(cx, cy - r * 0.55f), strokeWidth = 1.2.dp.toPx())
    // minute hand
    drawLine(color = color, start = Offset(cx, cy), end = Offset(cx + r * 0.4f, cy + r * 0.25f), strokeWidth = 1.2.dp.toPx())
}
