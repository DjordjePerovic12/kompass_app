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
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
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
                            0.5f to Color.Transparent,
                            1.0f to colors.colorNavy.copy(alpha = 0.75f)
                        )
                    )
                )
        )
    }
}
