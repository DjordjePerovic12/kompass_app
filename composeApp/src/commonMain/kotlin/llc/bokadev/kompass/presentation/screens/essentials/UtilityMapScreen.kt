package llc.bokadev.kompass.presentation.screens.essentials

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.buildMapsDirectionsUrlForCoords
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.shared.NativeUtilityDetailMapView
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import llc.bokadev.kompass.presentation.theme.KompassTheme
import llc.bokadev.kompass.presentation.theme.colorDustySage
import llc.bokadev.kompass.presentation.theme.colorMain
import llc.bokadev.kompass.presentation.theme.colorMutedSky
import llc.bokadev.kompass.presentation.theme.colorRoseClay
import llc.bokadev.kompass.presentation.theme.colorSandGold
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UtilityMapScreen(
    vmKey: String = "utility-map",
    onBack: () -> Unit
) {
    val vm: UtilityMapViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        analytics.trackScreenView("utility_map")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Nearby support",
                title = state.category?.title() ?: "Utilities",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        UtilityMapScreenContent(
            state = state,
            onIntent = vm::onIntent
        )
    }
}

@Composable
private fun UtilityMapScreenContent(
    state: UtilityMapState,
    onIntent: (UtilityMapEvent) -> Unit
) {
    val colors = KompassTheme.colors
    val selectedItem = state.items.firstOrNull { it.utility.id == state.selectedUtilityId } ?: state.items.firstOrNull()
    val pins = state.items.map { UtilityMapPin(title = it.utility.name, point = it.point) }
    val uriHandler = LocalUriHandler.current

    when {
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Could not load nearby utilities", color = colors.colorSlate)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Retry",
                        color = colors.colorAmberDark,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onIntent(UtilityMapEvent.Retry) }
                    )
                }
            }
        }

        state.items.isEmpty() && !state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No nearby locations found", color = colors.colorSlate)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.colorHomeCanvas),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Choose the point you want, then continue in Maps only for that location.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        color = colors.colorSlate.copy(alpha = 0.82f)
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, colors.colorSlateGhost, RoundedCornerShape(24.dp))
                    ) {
                        NativeUtilityDetailMapView(
                            pins = pins,
                            currentLocation = state.currentLocation,
                            focusedPinTitle = selectedItem?.utility?.name,
                            onPinSelected = { pinTitle ->
                                state.items.firstOrNull { it.utility.name == pinTitle }?.let { item ->
                                    onIntent(UtilityMapEvent.SelectUtility(item.utility.id))
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                selectedItem?.let { item ->
                    item {
                        SelectedUtilityCard(
                            item = item,
                            accent = item.utility.category.accentColor(colors),
                            onNavigate = {
                                uriHandler.openUri(
                                    buildMapsDirectionsUrlForCoords(
                                        item.point.latitude,
                                        item.point.longitude
                                    )
                                )
                            }
                        )
                    }
                }

                item {
                    Text(
                        text = "Nearby locations",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        ),
                        color = colors.colorNavy
                    )
                }

                items(state.items, key = { it.utility.id }) { item ->
                    UtilityLocationRow(
                        item = item,
                        selected = item.utility.id == selectedItem?.utility?.id,
                        accent = item.utility.category.accentColor(colors),
                        onSelect = { onIntent(UtilityMapEvent.SelectUtility(item.utility.id)) },
                        onNavigate = {
                            uriHandler.openUri(
                                buildMapsDirectionsUrlForCoords(
                                    item.point.latitude,
                                    item.point.longitude
                                )
                            )
                        }
                    )
                }

                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}

@Composable
private fun SelectedUtilityCard(
    item: UtilityLocationItem,
    accent: Color,
    onNavigate: () -> Unit
) {
    val colors = KompassTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.colorWhite)
            .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = item.utility.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 24.sp
            ),
            color = colors.colorNavy
        )
        item.utility.address?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = colors.colorSlate
            )
        }
        Text(
            text = item.distanceMeters?.prettyDistance() ?: "Nearby",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            color = accent
        )
        Text(
            text = "Navigate",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = accent,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onNavigate)
                .padding(top = 4.dp, bottom = 4.dp)
        )
    }
}

@Composable
private fun UtilityLocationRow(
    item: UtilityLocationItem,
    selected: Boolean,
    accent: Color,
    onSelect: () -> Unit,
    onNavigate: () -> Unit
) {
    val colors = KompassTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) colors.colorWhite else colors.colorSurface)
            .border(
                width = 1.dp,
                color = if (selected) accent.copy(alpha = 0.22f) else colors.colorSlateGhost.copy(alpha = 0.7f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (selected) accent else accent.copy(alpha = 0.35f))
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.utility.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = colors.colorNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val meta = listOfNotNull(
                item.distanceMeters?.prettyDistance(),
                item.utility.address?.takeIf { it.isNotBlank() }
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = colors.colorSlate.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = "Navigate",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = accent,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onNavigate)
                .padding(vertical = 4.dp)
        )
    }
}

private fun UtilityCategory.title(): String = when (this) {
    UtilityCategory.ATM -> "Cash & ATMs"
    UtilityCategory.PHARMACY -> "Pharmacies"
    UtilityCategory.SUPERMARKET -> "Markets"
    UtilityCategory.SHOP -> "Shops"
    UtilityCategory.PARKING -> "Parking"
    UtilityCategory.GAS_STATION -> "Gas Stations"
    UtilityCategory.EMERGENCY -> "Emergency"
}

private fun Int.prettyDistance(): String = when {
    this >= 1000 -> "${(this / 100.0).toInt() / 10.0}km"
    else -> "${this}m"
}

private fun UtilityCategory.accentColor(colors: llc.bokadev.kompass.presentation.theme.KompassColors): Color = when (this) {
    UtilityCategory.ATM -> colorDustySage
    UtilityCategory.PHARMACY -> colorRoseClay
    UtilityCategory.SUPERMARKET -> colorMutedSky
    UtilityCategory.SHOP -> colors.colorAmberDark
    UtilityCategory.PARKING -> colorSandGold
    UtilityCategory.GAS_STATION -> colorMain
    UtilityCategory.EMERGENCY -> Color(0xFFB65246)
}
