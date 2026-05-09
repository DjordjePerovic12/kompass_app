package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.shared.FavoriteToggleButton
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun FeaturedPlaceCard(
    name: String,
    meta: String,
    imageUrl: String? = null,
    isFavorited: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    MustSeeCarouselCard(
        name = name,
        meta = meta,
        imageUrl = imageUrl,
        isFavorited = isFavorited,
        onFavoriteClick = onFavoriteClick,
        onClick = onClick,
        height = 318.dp,
        titleSize = 31.sp,
        titleLineHeight = 35.sp,
        metaSize = 13.sp
    )
}

@Composable
fun MustSeeMiniCard(
    name: String,
    meta: String,
    imageUrl: String? = null,
    isFavorited: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    MustSeeCarouselCard(
        name = name,
        meta = meta,
        imageUrl = imageUrl,
        isFavorited = isFavorited,
        onFavoriteClick = onFavoriteClick,
        onClick = onClick,
        height = 318.dp,
        titleSize = 26.sp,
        titleLineHeight = 30.sp,
        metaSize = 12.sp
    )
}

@Composable
fun MustSeeCarouselCard(
    name: String,
    meta: String,
    imageUrl: String?,
    isFavorited: Boolean,
    onFavoriteClick: (() -> Unit)?,
    onClick: () -> Unit,
    height: androidx.compose.ui.unit.Dp,
    titleSize: androidx.compose.ui.unit.TextUnit,
    titleLineHeight: androidx.compose.ui.unit.TextUnit,
    metaSize: androidx.compose.ui.unit.TextUnit
) {
    val colors = KompassTheme.colors
    val cardShape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(
                elevation = 14.dp,
                shape = cardShape,
                ambientColor = colors.colorNavy.copy(alpha = 0.14f),
                spotColor = colors.colorNavy.copy(alpha = 0.20f)
            )
            .clip(cardShape)
            .border(1.dp, colors.colorWhite.copy(alpha = 0.16f), cardShape)
            .clickable(onClick = onClick)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(colors.colorNavy))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to colors.colorNavy.copy(alpha = 0.10f),
                            0.38f to colors.colorNavy.copy(alpha = 0.14f),
                            0.68f to colors.colorNavy.copy(alpha = 0.42f),
                            1.0f to colors.colorNavy.copy(alpha = 0.82f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.56f to Color.Transparent,
                            1.0f to colors.colorOrangeMain.copy(alpha = 0.18f)
                        )
                    )
                )
        )

        if (onFavoriteClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                FavoriteToggleButton(
                    isFavorited = isFavorited,
                    onClick = onFavoriteClick,
                    size = 38.dp
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = meta,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = metaSize,
                    letterSpacing = 0.15.sp
                ),
                color = colors.colorWhite.copy(alpha = 0.86f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = titleSize,
                    lineHeight = titleLineHeight
                ),
                color = colors.colorWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
