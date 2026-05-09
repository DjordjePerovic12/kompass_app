package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.shared.FavoriteToggleButton
import llc.bokadev.kompass.presentation.theme.KompassTheme
import androidx.compose.foundation.Canvas

@Composable
fun NearbyDiscoveryCard(
    name: String,
    category: String,
    zone: String,
    distance: String,
    meta: String,
    imageUrl: String? = null,
    isFavorited: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(24.dp)
    val cardBackground by animateColorAsState(
        targetValue = if (pressed) colors.colorWhite.copy(alpha = 0.92f) else colors.colorWhite,
        animationSpec = tween(150),
        label = "nearby_discovery_bg"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 7.dp,
                shape = shape,
                ambientColor = colors.colorNavy.copy(alpha = 0.06f),
                spotColor = colors.colorNavy.copy(alpha = 0.10f)
            )
            .clip(shape)
            .background(cardBackground)
            .border(1.dp, colors.colorNavy.copy(alpha = 0.08f), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.colorNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$distance · $zone",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    ),
                    color = colors.colorSlate.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = CircleShape,
                        ambientColor = colors.colorNavy.copy(alpha = 0.05f),
                        spotColor = colors.colorNavy.copy(alpha = 0.08f)
                    )
                    .clip(CircleShape)
                    .background(colors.colorWhite)
                    .border(1.dp, colors.colorNavy.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(22.dp)) {
                    drawLine(
                        color = colors.colorNavy,
                        start = Offset(size.width * 0.22f, size.height * 0.78f),
                        end = Offset(size.width * 0.78f, size.height * 0.22f),
                        strokeWidth = 1.8.dp.toPx()
                    )
                    drawLine(
                        color = colors.colorNavy,
                        start = Offset(size.width * 0.48f, size.height * 0.22f),
                        end = Offset(size.width * 0.78f, size.height * 0.22f),
                        strokeWidth = 1.8.dp.toPx()
                    )
                    drawLine(
                        color = colors.colorNavy,
                        start = Offset(size.width * 0.78f, size.height * 0.22f),
                        end = Offset(size.width * 0.78f, size.height * 0.52f),
                        strokeWidth = 1.8.dp.toPx()
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.colorNavy.copy(alpha = 0.06f))
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.10f))
                )
            }
        }

        Text(
            text = "$category · $meta",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            color = colors.colorSlate.copy(alpha = 0.66f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (onFavoriteClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .offset(y = (-6).dp)
            ) {
                FavoriteToggleButton(
                    isFavorited = isFavorited,
                    onClick = onFavoriteClick,
                    size = 38.dp
                )
            }
        }
    }
}
