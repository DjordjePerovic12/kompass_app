package llc.bokadev.kompass.presentation.screens.category_items_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.presentation.shared.FavoriteToggleButton
import llc.bokadev.kompass.presentation.theme.KompassTheme
import llc.bokadev.kompass.presentation.theme.colorBlack

@Composable
fun PlaceListItem(
    place: Place,
    lang: String = "en",
    topMetaOverride: String? = null,
    bottomMetaOverride: String? = null,
    isFavorited: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val zoneLabel = place.zone.prettyPlaceLabel()
    val subCategoryLabel = place.subCategory.prettyPlaceLabel()
    val topMeta = topMetaOverride ?: listOfNotNull(
        place.category.uiLabel(),
        zoneLabel.takeIf { it.isNotBlank() }
    ).joinToString(" · ")
    val bottomMeta = bottomMetaOverride ?: listOfNotNull(
        place.estimatedDuration?.formatDuration()?.let { "$it walk" },
        subCategoryLabel.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = shape,
                ambientColor = colorBlack.copy(alpha = 0.04f),
                spotColor = colorBlack.copy(alpha = 0.08f)
            )
            .clip(shape)
            .clickable(onClick = onClick),
        color = colors.colorWhite,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.colorSignal.copy(alpha = 0.06f), shape)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(104.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.colorSlateGhost)
            ) {
                val imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) }
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = place.localizedName(lang),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorBlack.copy(alpha = 0.06f))
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.colorSignal.copy(alpha = 0.18f))
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (topMeta.isNotBlank()) {
                        Text(
                            text = topMeta,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = colors.colorSignalStrong.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = place.localizedName(lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            lineHeight = 24.sp
                        ),
                        color = colors.colorNavy,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (bottomMeta.isNotBlank()) {
                        Text(
                            text = bottomMeta,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.colorSlateLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                onFavoriteClick?.let {
                    FavoriteToggleButton(
                        isFavorited = isFavorited,
                        onClick = it,
                        size = 36.dp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.colorWhite)
                        .border(1.dp, colors.colorSignal.copy(alpha = 0.08f), RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↗",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.colorNavy
                    )
                }
            }
        }
    }
}

private fun Int.formatDuration(): String = when {
    this < 60  -> "${this} min"
    this % 60 == 0 -> "${this / 60}h"
    else -> "${this / 60}h ${this % 60}min"
}

private fun String?.prettyPlaceLabel(): String =
    this?.split('_', '-', ' ')
        ?.filter { it.isNotBlank() }
        ?.joinToString(" ") { token ->
            token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        ?: ""

private fun PlaceCategory.uiLabel(): String = when (this) {
    PlaceCategory.EAT_AND_DRINK -> "Eat & Drink"
    PlaceCategory.SEE_AND_VISIT -> "See & Visit"
    PlaceCategory.ACTIVITIES -> "Activities"
    PlaceCategory.HIDDEN_GEMS -> "Hidden Gems"
    PlaceCategory.PRACTICAL -> "Practical"
}
