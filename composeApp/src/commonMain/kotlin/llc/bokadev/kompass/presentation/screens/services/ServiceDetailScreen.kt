package llc.bokadev.kompass.presentation.screens.services

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import llc.bokadev.kompass.core.util.buildMapsUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Service
import llc.bokadev.kompass.presentation.screens.placedetail.components.InfoChip
import llc.bokadev.kompass.presentation.shared.DetailNativeMapCard
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ServiceDetailScreen(
    id: String,
    onBack: () -> Unit
) {
    val vm: ServiceDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()
    val colors = KompassTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorSurface)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.colorOrangeMain
                )
            }

            state.error != null -> {
                Text(
                    text = state.error ?: "Could not load service",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorNavy
                )
            }

            state.service != null -> {
                val loadedService = state.service ?: return@Box
                ServiceDetailBody(
                    service = loadedService,
                    onBack = onBack
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceDetailBody(
    service: Service,
    onBack: () -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()
    val uriHandler = LocalUriHandler.current
    val location = service.localizedLocation(lang)
    val chips = listOfNotNull(
        "Service",
        location?.takeIf { it.isNotBlank() },
        service.externalWebsite?.let { "Website" }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box {
            ServiceHeroHeader()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServiceOverlayCircleButton(symbol = "‹", onClick = onBack)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-30).dp)
                .padding(horizontal = 14.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.14f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(colors.colorWhite)
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = service.localizedName(lang),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 31.sp,
                        lineHeight = 35.sp,
                        letterSpacing = (-0.5).sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.colorNavy
                )
                Text(
                    text = location ?: strings.servicesSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorNavy.copy(alpha = 0.62f)
                )
            }

            if (chips.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chips.forEachIndexed { index, chip ->
                        InfoChip(label = chip, amber = index == 0)
                    }
                }
            }

            if (service.latitude != null && service.longitude != null) {
                DetailNativeMapCard(
                    placeName = service.localizedName(lang),
                    summary = "Find ${service.localizedName(lang)} and explore practical stops around it.",
                    destination = GeoPoint(service.latitude, service.longitude)
                )
            }

            ServiceDetailSection(
                title = "Description",
                body = service.localizedDescription(lang)
            )

            location?.takeIf { it.isNotBlank() }?.let { label ->
                ServiceFactCard(
                    label = "Location",
                    value = label,
                    action = { uriHandler.openUri(buildMapsUrl(label)) },
                    actionText = "Open in Maps"
                )
            }

            service.externalWebsite?.let { url ->
                ServiceFactCard(
                    label = "Website",
                    value = url,
                    action = { uriHandler.openUri(url) },
                    actionText = "Visit website"
                )
            }
        }

        Spacer(modifier = Modifier.height(34.dp))
    }
}

@Composable
private fun ServiceHeroHeader() {
    val colors = KompassTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(284.dp)
            .background(colors.colorOrangeMain)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(132.dp)
                .clip(RoundedCornerShape(38.dp))
                .background(colors.colorWhite.copy(alpha = 0.14f))
                .border(
                    1.dp,
                    colors.colorWhite.copy(alpha = 0.18f),
                    RoundedCornerShape(38.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(56.dp)) {
                drawServiceIcon(colors.colorWhite)
            }
        }
    }
}

@Composable
private fun ServiceOverlayCircleButton(
    symbol: String,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(colors.colorWhite.copy(alpha = 0.18f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium,
            color = colors.colorWhite
        )
    }
}

@Composable
private fun ServiceDetailSection(
    title: String,
    body: String
) {
    val colors = KompassTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
            color = colors.colorNavy.copy(alpha = 0.78f)
        )
    }
}

@Composable
private fun ServiceFactCard(
    label: String,
    value: String,
    action: () -> Unit,
    actionText: String
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.colorSurface)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorOrangeMain
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = colors.colorNavy
        )
        Text(
            text = actionText,
            modifier = Modifier.clickable(onClick = action),
            style = MaterialTheme.typography.bodySmall,
            color = colors.colorOrangeMain
        )
    }
}
