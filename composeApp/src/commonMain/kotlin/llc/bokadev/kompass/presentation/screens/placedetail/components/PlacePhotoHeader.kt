package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun PlacePhotoHeader(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    imageAspectRatio: Float? = 4f / 3f
) {
    val colors = KompassTheme.colors
    val validImages = imageUrls
        .map(String::trim)
        .filter { it.isNotBlank() }
        .distinct()
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
        if (validImages.isNotEmpty()) {
            if (validImages.size == 1) {
                AsyncImage(
                    model = validImages.first(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val pagerState = rememberPagerState(pageCount = { validImages.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = validImages[page],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 18.dp, end = 18.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.34f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${validImages.size}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(validImages.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                                .background(
                                    color = if (index == pagerState.currentPage) {
                                        Color.White.copy(alpha = 0.94f)
                                    } else {
                                        Color.White.copy(alpha = 0.42f)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
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
