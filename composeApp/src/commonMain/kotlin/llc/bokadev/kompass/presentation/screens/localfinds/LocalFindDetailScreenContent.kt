package llc.bokadev.kompass.presentation.screens.localfinds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.shared.DetailNativeMapCard
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun LocalFindDetailScreenContent(
    state: LocalFindDetailState,
    onIntent: (LocalFindDetailEvent) -> Unit,
    onBack: () -> Unit
) {
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
                    text = state.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .clickable { onIntent(LocalFindDetailEvent.Retry) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorNavy
                )
            }

            state.find != null -> {
                LocalFindDetailBody(
                    find = state.find,
                    hasDeepAccess = state.hasDeepAccess,
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
private fun LocalFindDetailBody(
    find: LocalFind,
    hasDeepAccess: Boolean,
    onBack: () -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Photo header with back button overlay
            Box {
                PlacePhotoHeader(
                    imageUrls = find.photos,
                    imageAspectRatio = 0.94f
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    OverlayCircleButton(symbol = "‹", onClick = onBack)
                }
            }

            // Floating content card
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
                // Title block
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Kotor Legend",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.colorAmberDark
                    )
                    Text(
                        text = find.localizedTitle(lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 31.sp,
                            lineHeight = 35.sp,
                            letterSpacing = (-0.5).sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.colorNavy
                    )
                    val location = find.localizedLocation(lang)
                    if (location.isNotBlank()) {
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.colorNavy.copy(alpha = 0.62f)
                        )
                    }
                }

                // Map card if coordinates are present
                if (find.latitude != null && find.longitude != null) {
                    DetailNativeMapCard(
                        placeName = find.localizedTitle(lang),
                        summary = find.localizedLocation(lang).ifBlank { "Location of this legend in Kotor" },
                        destination = GeoPoint(find.latitude, find.longitude)
                    )
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))

                // Story body
                DetailSection(
                    title = "The Legend",
                    body = find.localizedBody(lang)
                )

                // Location fact card if there's a text location
                val location = find.localizedLocation(lang)
                if (location.isNotBlank()) {
                    DetailFactCard(
                        label = "Where",
                        value = location
                    )
                }

                // Local knowledge callout card
                LocalKnowledgeCard()
            }

            Spacer(modifier = Modifier.height(34.dp))
        }
    }
}

@Composable
private fun OverlayCircleButton(symbol: String, onClick: () -> Unit) {
    val colors = KompassTheme.colors
    Box(
        modifier = Modifier
            .size(42.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.16f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(CircleShape)
            .background(colors.colorWhite)
            .border(1.dp, Color.Black.copy(alpha = 0.06f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp
            ),
            color = colors.colorNavy
        )
    }
}

@Composable
private fun DetailSection(title: String, body: String) {
    val colors = KompassTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
            color = colors.colorNavy.copy(alpha = 0.76f)
        )
    }
}

@Composable
private fun DetailFactCard(label: String, value: String) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.colorSurface)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy.copy(alpha = 0.56f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.colorNavy
        )
    }
}

@Composable
private fun LocalKnowledgeCard(
    title: String = "Local knowledge",
    body: String = "This story has been passed down through generations in the Bay of Kotor. Ask a local — they'll have their own version.",
    cta: String? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.colorAmber.copy(alpha = 0.08f))
            .border(1.dp, colors.colorAmber.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 22.dp, height = 3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.colorAmberDark)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorAmberDark
            )
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
            color = colors.colorNavy.copy(alpha = 0.82f)
        )
        if (cta != null && onClick != null) {
            Text(
                text = cta,
                modifier = Modifier.clickable(onClick = onClick),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorOrangeMain
            )
        }
    }
}

@Composable
private fun DeepCompanionSection(
    title: String,
    body: String,
    cta: String? = null,
    onClick: (() -> Unit)? = null,
    isLocked: Boolean = false
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy
            )
            if (isLocked) {
                Text(
                    text = "Optional layer",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.colorOrangeMain.copy(alpha = 0.88f)
                )
            }
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = colors.colorNavy.copy(alpha = 0.78f)
        )
        if (cta != null && onClick != null) {
            Text(
                text = cta,
                modifier = Modifier.clickable(onClick = onClick),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorOrangeMain
            )
        }
    }
}
