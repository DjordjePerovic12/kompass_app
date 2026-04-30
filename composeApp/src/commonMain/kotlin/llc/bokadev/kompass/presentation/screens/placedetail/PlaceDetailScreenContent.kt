package llc.bokadev.kompass.presentation.screens.placedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PriceIndicator
import llc.bokadev.kompass.presentation.screens.placedetail.components.InfoChip
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun PlaceDetailScreenContent(
    state: PlaceDetailState,
    onIntent: (PlaceDetailEvent) -> Unit,
    onBack: () -> Unit
) {
    val colors = KompassTheme.colors

    Box(modifier = Modifier.fillMaxSize().background(colors.colorSurface)) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.colorAmber
                )
            }
            state.error != null -> {
                Text(
                    text = state.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorError
                )
            }
            state.place != null -> {
                PlaceDetailBody(place = state.place, onBack = onBack)
            }
        }
    }
}

@Composable
private fun PlaceDetailBody(place: Place, onBack: () -> Unit) {
    val colors = KompassTheme.colors

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Photo (4:3 ratio with gradient overlay)
            PlacePhotoHeader(
                imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) }
            )

            // White card — overlaps the photo by 24dp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp)
                    .background(colors.colorWhite, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 48.dp)
            ) {
                // Place name
                Text(
                    text = place.localizedName("en"),
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.colorNavy
                )

                Spacer(Modifier.height(12.dp))

                // Info chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(label = place.category.uiText)
                    place.zone?.let { InfoChip(label = it) }
                    place.priceIndicator?.let { price ->
                        when (price) {
                            PriceIndicator.BUDGET   -> InfoChip(label = "Free entry", amber = true)
                            PriceIndicator.MODERATE -> InfoChip(label = "€€")
                            PriceIndicator.EXPENSIVE -> InfoChip(label = "€€€")
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = colors.colorSlateGhost)
                Spacer(Modifier.height(20.dp))

                // About section
                Text(
                    text = "About",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.colorNavy
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = place.localizedDescription("en"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate
                )

                // Local's Tip
                val tip = place.localizedLocalsTip("en")
                if (tip.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    LocalsTipCard(tip = tip)
                }
            }
        }

        // Fixed back button (overlaid, not scrollable)
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
                .align(Alignment.TopStart)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "‹",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun LocalsTipCard(tip: String) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorAmberSubtle, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(colors.colorAmber, CircleShape)
            )
            Text(
                text = "Local's Tip",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorAmberDark
            )
        }
        Text(
            text = tip,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.colorNavy
        )
    }
}
