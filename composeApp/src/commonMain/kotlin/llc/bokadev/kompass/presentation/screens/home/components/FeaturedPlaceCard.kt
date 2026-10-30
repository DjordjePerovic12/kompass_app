package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.shared.FavoriteToggleButton
import llc.bokadev.kompass.presentation.theme.HomePalette
import llc.bokadev.kompass.presentation.theme.helveticaMedium16
import llc.bokadev.kompass.presentation.theme.helveticaRegular14

/**
 * A portrait photographic tile for the Must See row. Title (and metadata) sit inside the
 * photo at the bottom behind a dark scrim — no separate white caption area. A missing URL *or*
 * a photo that fails to load both fall back to [HomeImageFallback]'s solid, readable surface —
 * never a scrim drawn over a blank/failed image.
 */
@Composable
fun FeaturedPlaceCard(
    name: String,
    meta: String,
    imageUrl: String? = null,
    isFavorited: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasUrl = !imageUrl.isNullOrBlank()
    var loadFailed by remember(imageUrl) { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(shape)
            .border(1.dp, HomePalette.surface, shape)
            .clickable(onClick = onClick)
    ) {
        if (hasUrl && !loadFailed) {
            Box(modifier = Modifier.fillMaxSize().background(HomePalette.selection.copy(alpha = 0.35f)))
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { loadFailed = true },
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.55f to Color.Transparent,
                                1f to HomePalette.ink.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = name,
                    style = helveticaMedium16().copy(fontSize = 17.sp, lineHeight = 22.sp),
                    color = HomePalette.surface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = helveticaRegular14(),
                        color = HomePalette.surface.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            HomeImageFallback(label = name, secondaryLabel = meta.takeIf { it.isNotBlank() }, modifier = Modifier.fillMaxSize())
        }

        if (onFavoriteClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                FavoriteToggleButton(
                    isFavorited = isFavorited,
                    onClick = onFavoriteClick,
                    size = 36.dp
                )
            }
        }
    }
}
