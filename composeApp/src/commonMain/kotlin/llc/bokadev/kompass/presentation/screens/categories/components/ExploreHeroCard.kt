package llc.bokadev.kompass.presentation.screens.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/**
 * Explore's Places hero (spec §2.2): a 230dp editorial photo card with a bottom scrim, Gloock
 * title, subtitle, and a decorative lamp "go" circle. With no photo, or a failed one, it becomes
 * a plain raised card with the same title and button, never an empty photo frame.
 */
@Composable
fun ExploreHeroCard(
    title: String,
    subtitle: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LamplightTheme.colors
    val shape = RoundedCornerShape(24.dp)
    var failed by remember(imageUrl) { mutableStateOf(false) }
    val hasPhoto = !imageUrl.isNullOrBlank() && !failed

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 230.dp)
            .clip(shape)
            .background(if (hasPhoto) colors.surfaceMuted else colors.surfaceRaised)
            .then(if (hasPhoto) Modifier else Modifier.border(1.dp, colors.divider, shape))
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = "$title, $subtitle"
                onClick { onClick(); true }
            }
    ) {
        if (hasPhoto) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { failed = true },
                modifier = Modifier.matchParentSize()
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.4f to Color.Transparent,
                            1f to colors.night.copy(alpha = 0.94f)
                        )
                    )
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = title, style = LamplightType.sectionTitle().copy(fontSize = 36.sp, lineHeight = 38.sp), color = colors.textPrimary)
                Text(text = subtitle, style = LamplightType.body().copy(fontSize = 15.sp), color = Color(0xFFC3C7BF))
            }
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(colors.lamp),
                contentAlignment = Alignment.Center
            ) {
                ExploreIcon(ExploreGlyph.ArrowRight, color = colors.night, size = 18.dp)
            }
        }
    }
}

@Preview
@Composable
private fun ExploreHeroCardPreview() {
    ExploreHeroCard(title = "Places", subtitle = "Heritage and viewpoints", imageUrl = null, onClick = {})
}
