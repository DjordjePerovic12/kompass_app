package llc.bokadev.kompass.presentation.screens.essentials

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.buildMapsUrl
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun EssentialsScreenContent(
    state: EssentialsState,
    onIntent: (EssentialsEvent) -> Unit
) {
    val colors = KompassTheme.colors

    when {
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Could not load essentials", color = colors.colorSlate)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Retry",
                        color = colors.colorAmberDark,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onIntent(EssentialsEvent.Retry) }
                    )
                }
            }
        }

        state.groupedEssentials.isEmpty() && !state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No essentials available", color = colors.colorSlate)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.colorSurface)
            ) {
                CATEGORY_ORDER.forEach { category ->
                    val items = state.groupedEssentials[category] ?: return@forEach

                    item(key = "header_${category.name}") {
                        CategorySectionHeader(category = category)
                    }

                    items(items, key = { it.id }) { essential ->
                        EssentialCard(
                            essential = essential,
                            expanded = essential.id in state.expandedIds,
                            onClick = { onIntent(EssentialsEvent.ToggleItem(essential.id)) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = colors.colorSlateGhost
                        )
                    }

                    item(key = "spacer_${category.name}") { Spacer(Modifier.height(8.dp)) }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun CategorySectionHeader(category: EssentialCategory) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(category.accentColor(colors))
        )
        Text(
            text = category.label().uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.8.sp
            ),
            color = colors.colorSlateLight
        )
    }
}

@Composable
private fun EssentialCard(
    essential: CityEssential,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.colorSurface)
            .border(1.dp, colors.colorSlatePale, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = essential.localizedTitle("en"),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Canvas(modifier = Modifier.size(20.dp)) {
                if (expanded) drawChevronUp(colors.colorNavyMuted)
                else drawChevronDown(colors.colorNavyMuted)
            }
        }

        if (expanded) {
            Text(
                text = essential.localizedContent("en"),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.colorSlate
            )

            essential.localizedLocation("en")?.let { location ->
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.clickable {
                        uriHandler.openUri(buildMapsUrl(location))
                    },
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(12.dp)) { drawPin(colors.colorAmberDark) }
                    Text(
                        text = location,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.colorAmberDark
                    )
                }
            }

            if (essential.links.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                essential.links.forEach { link ->
                    Text(
                        text = link,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.colorAmberDark,
                        modifier = Modifier.clickable { uriHandler.openUri(link) }
                    )
                }
            }
        }
    }
}

private val CATEGORY_ORDER = listOf(
    EssentialCategory.TRANSPORT,
    EssentialCategory.CUSTOMS,
    EssentialCategory.TIPS,
    EssentialCategory.PRACTICAL,
    EssentialCategory.EMERGENCY
)

private fun EssentialCategory.label(): String = when (this) {
    EssentialCategory.TRANSPORT -> "Getting Around"
    EssentialCategory.CUSTOMS   -> "Local Customs"
    EssentialCategory.EMERGENCY -> "Emergency"
    EssentialCategory.TIPS      -> "Tips & Advice"
    EssentialCategory.PRACTICAL -> "Practical Info"
}

private fun EssentialCategory.accentColor(colors: llc.bokadev.kompass.presentation.theme.KompassColors): Color = when (this) {
    EssentialCategory.TRANSPORT -> colors.colorSlateLight
    EssentialCategory.CUSTOMS   -> colors.colorEmber
    EssentialCategory.EMERGENCY -> colors.colorError
    EssentialCategory.TIPS      -> colors.colorSuccess
    EssentialCategory.PRACTICAL -> colors.colorAmberDark
}

private fun DrawScope.drawChevronDown(color: Color) {
    val path = Path().apply {
        moveTo(size.width * 0.15f, size.height * 0.38f)
        lineTo(size.width * 0.5f,  size.height * 0.65f)
        lineTo(size.width * 0.85f, size.height * 0.38f)
    }
    drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
}

private fun DrawScope.drawChevronUp(color: Color) {
    val path = Path().apply {
        moveTo(size.width * 0.15f, size.height * 0.62f)
        lineTo(size.width * 0.5f,  size.height * 0.35f)
        lineTo(size.width * 0.85f, size.height * 0.62f)
    }
    drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
}

private fun DrawScope.drawPin(color: Color) {
    val cx = size.width / 2f
    drawCircle(color = color, radius = size.width * 0.3f, center = Offset(cx, size.height * 0.32f))
    val path = Path().apply {
        moveTo(cx - size.width * 0.18f, size.height * 0.42f)
        lineTo(cx, size.height * 0.92f)
        lineTo(cx + size.width * 0.18f, size.height * 0.42f)
        close()
    }
    drawPath(path, color)
}
