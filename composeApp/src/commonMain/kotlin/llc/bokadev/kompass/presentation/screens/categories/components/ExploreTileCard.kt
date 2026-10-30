package llc.bokadev.kompass.presentation.screens.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/** Which family a tile represents; the fills differ on purpose so the two read apart at a glance. */
enum class ExploreTileStyle { Limestone, Raised }

/**
 * Half-width Explore entry (spec §2.3): Eat & Drink on `limestone`, Activities on `surfaceRaised`.
 * 190dp minimum; grows with large fonts, and never truncates its title.
 */
@Composable
fun ExploreTileCard(
    title: String,
    subtitle: String,
    glyph: ExploreGlyph,
    style: ExploreTileStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LamplightTheme.colors
    val shape = RoundedCornerShape(24.dp)
    val limestone = style == ExploreTileStyle.Limestone
    Column(
        modifier = modifier
            .heightIn(min = 190.dp)
            .clip(shape)
            .background(if (limestone) colors.limestone else colors.surfaceRaised)
            .then(if (limestone) Modifier else Modifier.border(1.dp, colors.divider, shape))
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = "$title, $subtitle"
                onClick { onClick(); true }
            }
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ExploreIcon(glyph, color = if (limestone) colors.night else colors.lamp, size = 28.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = LamplightType.sectionTitleS(),
                color = if (limestone) colors.night else colors.textPrimary
            )
            Text(
                text = subtitle,
                style = LamplightType.bodyS().copy(fontSize = 14.sp),
                color = if (limestone) colors.textOnLimestone else colors.textSecondary
            )
        }
    }
}

@Preview
@Composable
private fun ExploreTileCardPreview() {
    ExploreTileCard("Eat & Drink", "Restaurants and cafés", ExploreGlyph.ForkKnife, ExploreTileStyle.Limestone, {}, Modifier.width(170.dp))
}
