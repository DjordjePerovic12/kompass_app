package llc.bokadev.kompass.presentation.screens.experiences.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.presentation.shared.FavoriteToggleButton
import llc.bokadev.kompass.presentation.theme.KompassTheme
import llc.bokadev.kompass.presentation.theme.colorBlack

@Composable
fun ActivityListItem(
    activity: Experience,
    lang: String = "en",
    topMetaOverride: String? = null,
    bottomMetaOverride: String? = null,
    isFavorited: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val categoryLabel = activity.category.prettyActivityCategory()
    val locationLabel = activity.localizedLocation(lang).takeIf { it.isNotBlank() }
    val topMeta = topMetaOverride ?: listOfNotNull(categoryLabel, locationLabel).joinToString(" · ")
    val bottomMeta = bottomMetaOverride ?: listOfNotNull(
        activity.durationMin?.let { "$it min" },
        activity.bestTime.toUiLabel()
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
                val imageUrl = activity.photos.firstOrNull()?.let { buildPhotoUrl(it) }
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = activity.localizedName(lang),
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
                        text = activity.localizedName(lang),
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

private fun String?.prettyActivityCategory(): String? =
    this?.split('_', '-', ' ')
        ?.filter { it.isNotBlank() }
        ?.joinToString(" ") { token ->
            token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        ?.takeIf { it.isNotBlank() }

private fun BestTime.toUiLabel(): String = when (this) {
    BestTime.MORNING -> "Morning"
    BestTime.AFTERNOON -> "Afternoon"
    BestTime.EVENING -> "Evening"
    BestTime.ANYTIME -> "Any time"
}
