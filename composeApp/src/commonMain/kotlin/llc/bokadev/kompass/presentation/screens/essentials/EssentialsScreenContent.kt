package llc.bokadev.kompass.presentation.screens.essentials

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.buildMapsUrl
import llc.bokadev.kompass.core.util.buildNearbyUtilityMapsUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.openUtilityCategoryInMaps
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.presentation.shared.NativeUtilityPreviewMapView
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import llc.bokadev.kompass.presentation.theme.KompassColors
import llc.bokadev.kompass.presentation.theme.KompassTheme
import llc.bokadev.kompass.presentation.theme.colorDustySage
import llc.bokadev.kompass.presentation.theme.colorMain
import llc.bokadev.kompass.presentation.theme.colorMutedSky
import llc.bokadev.kompass.presentation.theme.colorRoseClay
import llc.bokadev.kompass.presentation.theme.colorSandGold

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

        state.sections.isEmpty() && state.groupedEssentials.isEmpty() && !state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No essentials available", color = colors.colorSlate)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.colorHomeCanvas),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    EssentialsModeSwitch(
                        selectedTab = state.selectedTab,
                        onSelect = { onIntent(EssentialsEvent.SelectTab(it)) }
                    )
                }

                when (state.selectedTab) {
                    EssentialsTab.UTILITY -> {
                        items(state.sections, key = { it.category.name }) { section ->
                            UtilityCategoryCard(
                                section = section,
                                currentLocation = if (section.showCurrentLocation) state.currentLocation else null
                            )
                        }
                    }

                    EssentialsTab.INFO -> {
                        CATEGORY_ORDER.forEach { category ->
                            val items = state.groupedEssentials[category] ?: return@forEach

                            item(key = "header_${category.name}") {
                                CategorySectionHeader(category = category)
                            }

                            items(items, key = { it.id }) { essential ->
                                EssentialInfoCard(
                                    essential = essential,
                                    expanded = essential.id in state.expandedIds,
                                    onClick = { onIntent(EssentialsEvent.ToggleItem(essential.id)) }
                                )
                            }

                            item(key = "spacer_${category.name}") { Spacer(Modifier.height(10.dp)) }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun EssentialsModeSwitch(
    selectedTab: EssentialsTab,
    onSelect: (EssentialsTab) -> Unit
) {
    val colors = KompassTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        EssentialsTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Text(
                text = if (tab == EssentialsTab.UTILITY) "Utilities" else "Essential Info",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = if (selected) colors.colorNavy else colors.colorSlate.copy(alpha = 0.8f),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) colors.colorWhite else colors.colorSurface)
                    .border(
                        width = 1.dp,
                        color = if (selected) colors.colorSlateGhost else colors.colorSlateGhost.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 11.dp)
            )
        }
    }
}

@Composable
private fun UtilityCategoryCard(
    section: UtilityCategorySection,
    currentLocation: GeoPoint?
) {
    val colors = KompassTheme.colors
    val uriHandler = LocalUriHandler.current
    val accent = section.category.accentColor(colors)
    val mapUrl = buildNearbyUtilityMapsUrl(
        categoryQuery = section.category.mapQuery(),
        centerLat = section.mapCenter.latitude,
        centerLng = section.mapCenter.longitude,
        currentLat = currentLocation?.latitude,
        currentLng = currentLocation?.longitude,
        fallbackLat = section.previewUtilities.firstOrNull()?.latitude,
        fallbackLng = section.previewUtilities.firstOrNull()?.longitude
    )

    val contextLine = when {
        section.nearbyCount > 0 && section.closestDistanceMeters != null ->
            "${section.nearbyCount} nearby • Closest ${section.closestDistanceMeters.prettyDistance()}"
        section.closestDistanceMeters != null ->
            "Closest ${section.closestDistanceMeters.prettyDistance()}"
        else -> "${section.totalCount} available"
    }

    val pins = section.previewUtilities.mapNotNull { utility ->
        val lat = utility.latitude ?: return@mapNotNull null
        val lng = utility.longitude ?: return@mapNotNull null
        UtilityMapPin(
            title = utility.name,
            point = GeoPoint(lat, lng)
        )
    }
    val openMapAction = {
        val handledNatively = openUtilityCategoryInMaps(
            categoryQuery = section.category.mapQuery(),
            centerLat = section.mapCenter.latitude,
            centerLng = section.mapCenter.longitude,
            currentLat = currentLocation?.latitude,
            currentLng = currentLocation?.longitude,
            fallbackLat = section.previewUtilities.firstOrNull()?.latitude,
            fallbackLng = section.previewUtilities.firstOrNull()?.longitude,
            pins = pins
        )
        if (!handledNatively) {
            uriHandler.openUri(mapUrl)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.colorWhite)
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent.copy(alpha = 0.18f))
                    .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(18.dp)) {
                    drawUtilityGlyph(section.category, accent)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = section.category.title(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.25).sp
                    ),
                    color = colors.colorNavy
                )
                Text(
                    text = contextLine,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    ),
                    color = colors.colorSlate.copy(alpha = 0.78f)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.colorSlateGhost, RoundedCornerShape(20.dp))
                .clickable(onClick = openMapAction)
        ) {
            NativeUtilityPreviewMapView(
                pins = pins,
                currentLocation = currentLocation,
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.previewUtilities.joinToString(" • ") { it.name }.take(56),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = colors.colorSlate.copy(alpha = 0.72f),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Open in Map",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = openMapAction)
                    .padding(start = 16.dp, top = 6.dp, end = 2.dp, bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun CategorySectionHeader(category: EssentialCategory) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
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
                letterSpacing = 1.6.sp
            ),
            color = colors.colorSlate.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun EssentialInfoCard(
    essential: CityEssential,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    val uriHandler = LocalUriHandler.current
    val lang = currentAppLanguage()
    val accent = essential.category.accentColor(colors)
    val hasMap = essential.latitude != null && essential.longitude != null
    val compactMeta = buildList {
        if (hasMap) add("Map")
        if (essential.links.isNotEmpty()) add("${essential.links.size} link${if (essential.links.size == 1) "" else "s"}")
    }.joinToString(" • ").ifBlank { essential.category.cardMetaLabel() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.colorWhite)
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent.copy(alpha = 0.18f))
                    .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(19.dp)) {
                    drawEssentialGlyph(essential.category, accent)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = essential.localizedTitle(lang),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    color = colors.colorNavy,
                    maxLines = if (expanded) 3 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = compactMeta.ifBlank { essential.category.label() },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        letterSpacing = (-0.1).sp
                    ),
                    color = colors.colorSlate.copy(alpha = 0.78f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Canvas(
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 6.dp)
            ) {
                if (expanded) drawChevronUp(colors.colorNavyMuted)
                else drawChevronDown(colors.colorNavyMuted)
            }
        }

        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.colorSlateGhost)
            )

            Text(
                text = essential.localizedContent(lang),
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 24.sp,
                    fontSize = 16.sp,
                    letterSpacing = (-0.05).sp
                ),
                color = colors.colorSlate.copy(alpha = 0.92f)
            )

            if (essential.latitude != null && essential.longitude != null) {
                EssentialDetailRow(
                    label = "Map",
                    value = "Open in Maps",
                    accent = accent,
                    onClick = { uriHandler.openUri(buildMapsUrl("${essential.latitude},${essential.longitude}")) }
                )
            }

            essential.links.forEach { link ->
                EssentialDetailRow(
                    label = "Link",
                    value = link.prettyLinkLabel(),
                    accent = accent,
                    onClick = { uriHandler.openUri(link) }
                )
            }
        }
    }
}

@Composable
private fun EssentialDetailRow(
    label: String,
    value: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KompassTheme.colors.colorSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            ),
            color = KompassTheme.colors.colorNavy
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 18.sp,
                fontStyle = FontStyle.Italic
            ),
            color = accent,
            modifier = Modifier.padding(start = 12.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val CATEGORY_ORDER = listOf(
    EssentialCategory.TRANSPORT,
    EssentialCategory.CUSTOMS,
    EssentialCategory.TIPS,
    EssentialCategory.PRACTICAL,
    EssentialCategory.EMERGENCY
)

private fun UtilityCategory.title(): String = when (this) {
    UtilityCategory.ATM -> "Cash & ATMs"
    UtilityCategory.PHARMACY -> "Pharmacies"
    UtilityCategory.SUPERMARKET -> "Markets"
    UtilityCategory.SHOP -> "Shops"
    UtilityCategory.PARKING -> "Parking"
    UtilityCategory.GAS_STATION -> "Gas Stations"
}

private fun UtilityCategory.mapQuery(): String = when (this) {
    UtilityCategory.ATM -> "ATM"
    UtilityCategory.PHARMACY -> "Pharmacy"
    UtilityCategory.SUPERMARKET -> "Supermarket"
    UtilityCategory.SHOP -> "Shop"
    UtilityCategory.PARKING -> "Parking"
    UtilityCategory.GAS_STATION -> "Gas Station"
}

private fun EssentialCategory.label(): String = when (this) {
    EssentialCategory.TRANSPORT -> "Getting Around"
    EssentialCategory.CUSTOMS -> "Local Customs"
    EssentialCategory.EMERGENCY -> "Emergency"
    EssentialCategory.TIPS -> "Tips & Advice"
    EssentialCategory.PRACTICAL -> "Practical Info"
}

private fun UtilityCategory.accentColor(colors: KompassColors): Color = when (this) {
    UtilityCategory.ATM -> colorDustySage
    UtilityCategory.PHARMACY -> colorRoseClay
    UtilityCategory.SUPERMARKET -> colorMutedSky
    UtilityCategory.SHOP -> colors.colorAmberDark
    UtilityCategory.PARKING -> colorSandGold
    UtilityCategory.GAS_STATION -> colorMain
}

private fun EssentialCategory.accentColor(colors: KompassColors): Color = when (this) {
    EssentialCategory.TRANSPORT -> colorMutedSky
    EssentialCategory.CUSTOMS -> colorRoseClay
    EssentialCategory.EMERGENCY -> colorMain
    EssentialCategory.TIPS -> colorDustySage
    EssentialCategory.PRACTICAL -> colorSandGold
}

private fun Int.prettyDistance(): String = when {
    this >= 1000 -> "${(this / 100.0).toInt() / 10.0}km"
    else -> "${this}m"
}

private fun DrawScope.drawChevronDown(color: Color) {
    val path = Path().apply {
        moveTo(size.width * 0.15f, size.height * 0.38f)
        lineTo(size.width * 0.5f, size.height * 0.65f)
        lineTo(size.width * 0.85f, size.height * 0.38f)
    }
    drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
}

private fun DrawScope.drawChevronUp(color: Color) {
    val path = Path().apply {
        moveTo(size.width * 0.15f, size.height * 0.62f)
        lineTo(size.width * 0.5f, size.height * 0.35f)
        lineTo(size.width * 0.85f, size.height * 0.62f)
    }
    drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
}

private fun DrawScope.drawUtilityGlyph(
    category: UtilityCategory,
    color: Color
) {
    when (category) {
        UtilityCategory.ATM -> {
            drawCircle(color = color, radius = size.minDimension * 0.18f, center = Offset(size.width * 0.3f, size.height * 0.72f))
            drawCircle(color = color, radius = size.minDimension * 0.18f, center = Offset(size.width * 0.7f, size.height * 0.72f))
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.24f, size.height * 0.58f)
                    lineTo(size.width * 0.36f, size.height * 0.3f)
                    lineTo(size.width * 0.64f, size.height * 0.3f)
                    lineTo(size.width * 0.76f, size.height * 0.58f)
                    close()
                },
                color = color,
                style = Stroke(width = 1.8.dp.toPx())
            )
        }
        UtilityCategory.PHARMACY -> {
            drawLine(color, Offset(size.width * 0.5f, size.height * 0.22f), Offset(size.width * 0.5f, size.height * 0.78f), 2.dp.toPx())
            drawLine(color, Offset(size.width * 0.22f, size.height * 0.5f), Offset(size.width * 0.78f, size.height * 0.5f), 2.dp.toPx())
        }
        UtilityCategory.SUPERMARKET -> {
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.34f)
                    lineTo(size.width * 0.74f, size.height * 0.34f)
                    lineTo(size.width * 0.66f, size.height * 0.7f)
                    lineTo(size.width * 0.26f, size.height * 0.7f)
                    close()
                },
                color = color,
                style = Stroke(width = 1.8.dp.toPx())
            )
            drawLine(color, Offset(size.width * 0.28f, size.height * 0.34f), Offset(size.width * 0.18f, size.height * 0.18f), 1.8.dp.toPx())
        }
        UtilityCategory.SHOP -> {
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.24f, size.height * 0.34f)
                    lineTo(size.width * 0.76f, size.height * 0.34f)
                    lineTo(size.width * 0.72f, size.height * 0.78f)
                    lineTo(size.width * 0.28f, size.height * 0.78f)
                    close()
                },
                color = color,
                style = Stroke(width = 1.8.dp.toPx())
            )
            drawLine(color, Offset(size.width * 0.36f, size.height * 0.24f), Offset(size.width * 0.36f, size.height * 0.42f), 1.8.dp.toPx())
            drawLine(color, Offset(size.width * 0.64f, size.height * 0.24f), Offset(size.width * 0.64f, size.height * 0.42f), 1.8.dp.toPx())
        }
        UtilityCategory.PARKING -> {
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.3f, size.height * 0.2f)
                    lineTo(size.width * 0.3f, size.height * 0.8f)
                },
                color = color,
                style = Stroke(width = 2.dp.toPx())
            )
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.3f, size.height * 0.22f)
                    lineTo(size.width * 0.56f, size.height * 0.22f)
                    cubicTo(size.width * 0.74f, size.height * 0.22f, size.width * 0.76f, size.height * 0.5f, size.width * 0.56f, size.height * 0.5f)
                    lineTo(size.width * 0.3f, size.height * 0.5f)
                },
                color = color,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        UtilityCategory.GAS_STATION -> {
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.28f)
                    lineTo(size.width * 0.58f, size.height * 0.28f)
                    lineTo(size.width * 0.58f, size.height * 0.76f)
                    lineTo(size.width * 0.28f, size.height * 0.76f)
                    close()
                },
                color = color,
                style = Stroke(width = 1.8.dp.toPx())
            )
            drawLine(color, Offset(size.width * 0.58f, size.height * 0.38f), Offset(size.width * 0.76f, size.height * 0.32f), 1.8.dp.toPx())
            drawLine(color, Offset(size.width * 0.76f, size.height * 0.32f), Offset(size.width * 0.76f, size.height * 0.66f), 1.8.dp.toPx())
        }
    }
}

private fun DrawScope.drawEssentialGlyph(category: EssentialCategory, color: Color) {
    when (category) {
        EssentialCategory.TRANSPORT -> {
            drawCircle(color = color, radius = size.minDimension * 0.16f, center = Offset(size.width * 0.28f, size.height * 0.72f))
            drawCircle(color = color, radius = size.minDimension * 0.16f, center = Offset(size.width * 0.72f, size.height * 0.72f))
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.55f)
                    lineTo(size.width * 0.32f, size.height * 0.3f)
                    lineTo(size.width * 0.68f, size.height * 0.3f)
                    lineTo(size.width * 0.8f, size.height * 0.55f)
                    close()
                },
                color = color,
                style = Stroke(width = 1.8.dp.toPx())
            )
        }
        EssentialCategory.CUSTOMS -> {
            drawCircle(color = color, radius = size.minDimension * 0.34f, style = Stroke(width = 1.8.dp.toPx()))
            drawLine(color = color, start = Offset(size.width * 0.3f, size.height * 0.52f), end = Offset(size.width * 0.7f, size.height * 0.52f), strokeWidth = 1.8.dp.toPx())
            drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.28f), end = Offset(size.width * 0.5f, size.height * 0.76f), strokeWidth = 1.8.dp.toPx())
        }
        EssentialCategory.EMERGENCY -> {
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.2f)
                    lineTo(size.width * 0.63f, size.height * 0.42f)
                    lineTo(size.width * 0.82f, size.height * 0.42f)
                    lineTo(size.width * 0.67f, size.height * 0.58f)
                    lineTo(size.width * 0.76f, size.height * 0.82f)
                    lineTo(size.width * 0.5f, size.height * 0.66f)
                    lineTo(size.width * 0.24f, size.height * 0.82f)
                    lineTo(size.width * 0.33f, size.height * 0.58f)
                    lineTo(size.width * 0.18f, size.height * 0.42f)
                    lineTo(size.width * 0.37f, size.height * 0.42f)
                    close()
                },
                color = color
            )
        }
        EssentialCategory.TIPS -> {
            drawCircle(color = color, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.34f))
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.42f, size.height * 0.52f)
                    lineTo(size.width * 0.58f, size.height * 0.52f)
                    lineTo(size.width * 0.64f, size.height * 0.74f)
                    lineTo(size.width * 0.36f, size.height * 0.74f)
                    close()
                },
                color = color
            )
        }
        EssentialCategory.PRACTICAL -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(size.width * 0.22f, size.height * 0.22f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.56f, size.height * 0.56f),
                style = Stroke(width = 1.8.dp.toPx())
            )
            drawLine(color = color, start = Offset(size.width * 0.34f, size.height * 0.4f), end = Offset(size.width * 0.66f, size.height * 0.4f), strokeWidth = 1.8.dp.toPx())
            drawLine(color = color, start = Offset(size.width * 0.34f, size.height * 0.56f), end = Offset(size.width * 0.58f, size.height * 0.56f), strokeWidth = 1.8.dp.toPx())
        }
    }
}

private fun EssentialCategory.cardMetaLabel(): String = when (this) {
    EssentialCategory.TRANSPORT -> "Routes & moving around"
    EssentialCategory.CUSTOMS -> "Local customs & etiquette"
    EssentialCategory.EMERGENCY -> "Important support info"
    EssentialCategory.TIPS -> "Advice for exploring well"
    EssentialCategory.PRACTICAL -> "Useful local information"
}

private fun String.prettyLinkLabel(): String {
    return removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .take(32)
        .ifBlank { "Open external link" }
}
