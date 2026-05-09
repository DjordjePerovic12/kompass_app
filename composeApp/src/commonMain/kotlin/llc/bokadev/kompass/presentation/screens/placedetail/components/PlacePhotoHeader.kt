package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun PlacePhotoHeader(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    imageAspectRatio: Float? = 4f / 3f
) {
    val colors = KompassTheme.colors
    val frameModifier = if (imageAspectRatio != null) {
        modifier
            .fillMaxWidth()
            .aspectRatio(imageAspectRatio)
    } else {
        modifier.fillMaxWidth()
    }
    Box(
        modifier = frameModifier
            .background(colors.colorSlateGhost)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.56f to Color.Transparent,
                            0.82f to Color.Black.copy(alpha = 0.32f),
                            1.0f to Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.72f to Color.Transparent,
                            1.0f to colors.colorOrangeMain.copy(alpha = 0.18f)
                        )
                    )
                )
        )
    }
}
