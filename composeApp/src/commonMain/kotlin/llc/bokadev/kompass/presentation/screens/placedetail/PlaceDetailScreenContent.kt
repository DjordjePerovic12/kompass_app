package llc.bokadev.kompass.presentation.screens.placedetail

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.model.PriceIndicator
import llc.bokadev.kompass.presentation.screens.placedetail.components.InfoChip
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.shared.FavoriteToggleButton
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun PlaceDetailScreenContent(
    state: PlaceDetailState,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    onIntent: (PlaceDetailEvent) -> Unit,
    onBack: () -> Unit,
    onLearnMore: () -> Unit,
    onOpenGuide: (String) -> Unit
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
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorNavy
                )
            }

            state.place != null -> {
                PlaceDetailBody(
                    place = state.place,
                    hasAudioAccess = state.hasAudioAccess,
                    hasDetailAccess = state.hasDetailAccess,
                    isFavorited = isFavorited,
                    onFavoriteClick = onFavoriteClick,
                    onBack = onBack,
                    onLearnMore = onLearnMore,
                    onOpenGuide = onOpenGuide
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlaceDetailBody(
    place: Place,
    hasAudioAccess: Boolean,
    hasDetailAccess: Boolean,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    onBack: () -> Unit,
    onLearnMore: () -> Unit,
    onOpenGuide: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val metaLine = listOfNotNull(
        place.subCategory?.prettyLabel(),
        place.zone?.prettyLabel()
    ).joinToString(" · ").ifBlank { place.category.prettyLabel() }

    val practicalLine = listOfNotNull(
        place.bestTime.toUiLabel().takeIf { it != "Any time" },
        place.estimatedDuration?.formatDuration(),
        place.priceIndicator?.toFriendlyLabel()
    ).joinToString(" · ")

    val chips = buildPlaceChips(place)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                PlacePhotoHeader(
                    imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) },
                    imageAspectRatio = 0.94f
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OverlayCircleButton(symbol = "‹", onClick = onBack)
                    FavoriteToggleButton(
                        isFavorited = isFavorited,
                        onClick = onFavoriteClick,
                        size = 42.dp
                    )
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
                        text = place.localizedName(lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 31.sp,
                            lineHeight = 35.sp,
                            letterSpacing = (-0.5).sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.colorNavy
                    )
                    Text(
                        text = metaLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.colorNavy.copy(alpha = 0.62f)
                    )
                    if (practicalLine.isNotBlank()) {
                        Text(
                            text = practicalLine,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = colors.colorOrangeMain
                        )
                    }
                }

                if (chips.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chips.forEachIndexed { index, chip ->
                            InfoChip(
                                label = chip,
                                amber = index == 0
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))

                DetailSection(
                    title = "Description",
                    body = place.localizedDescription(lang)
                )

                if (place.audioFile != null) {
                    PremiumGuideCard(
                        title = "Audio Guide",
                        body = if (hasAudioAccess) {
                            "This place includes an unlocked narration guide with background playback and location context."
                        } else {
                            "This narration is part of the Audio Pass and can be unlocked once premium access is active."
                        },
                        isLocked = !hasAudioAccess,
                        ctaLabel = if (hasAudioAccess) "OPEN GUIDE" else "LEARN MORE",
                        onPrimaryAction = {
                            if (hasAudioAccess) onOpenGuide(place.id) else onLearnMore()
                        }
                    )
                }

                place.localizedLongDescription(lang)
                    .takeIf { it.isNotBlank() }
                    ?.let { longText ->
                        PremiumGuideCard(
                            title = "Deep Dive",
                            body = if (hasDetailAccess) {
                                longText
                            } else {
                                "Deeper cultural context and editorial notes for this place are part of Explorer Pass content."
                            },
                            isLocked = !hasDetailAccess,
                            ctaLabel = if (hasDetailAccess) "READ GUIDE" else "LEARN MORE",
                            onPrimaryAction = {
                                if (hasDetailAccess) onOpenGuide(place.id) else onLearnMore()
                            }
                        )
                    }

                place.localizedLocalsTip(lang)
                    .takeIf { it.isNotBlank() }
                    ?.let { tip ->
                        LocalPerspectiveCard(tip = tip)
                    }

                place.openingHours
                    ?.values
                    ?.firstOrNull { it.isNotBlank() }
                    ?.let { hours ->
                        DetailFactCard(
                            label = "Practical",
                            value = hours
                        )
                    }
            }

            Spacer(modifier = Modifier.height(34.dp))
        }
    }
}

@Composable
private fun PremiumGuideCard(
    title: String,
    body: String,
    isLocked: Boolean,
    ctaLabel: String,
    onPrimaryAction: () -> Unit
) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (isLocked) colors.colorOrangeMain.copy(alpha = 0.08f) else colors.colorSurface)
            .border(
                width = 1.dp,
                color = if (isLocked) colors.colorOrangeMain.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isLocked) "$title · Premium" else title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = colors.colorNavy.copy(alpha = 0.78f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(colors.colorOrangeMain)
                .clickable(onClick = onPrimaryAction)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = ctaLabel,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorWhite
            )
        }
    }
}

@Composable
private fun OverlayCircleButton(
    symbol: String,
    onClick: (() -> Unit)? = null
) {
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
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
private fun DetailSection(
    title: String,
    body: String
) {
    val colors = KompassTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp
            ),
            color = colors.colorNavy.copy(alpha = 0.76f)
        )
    }
}

@Composable
private fun LocalPerspectiveCard(tip: String) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.colorOrangeMain.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = colors.colorOrangeMain.copy(alpha = 0.16f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 22.dp, height = 3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.colorOrangeMain)
            )
            Text(
                text = "Local perspective",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorOrangeMain
            )
        }
        Text(
            text = tip,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
            color = colors.colorNavy.copy(alpha = 0.82f)
        )
    }
}

@Composable
private fun DetailFactCard(
    label: String,
    value: String
) {
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
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.colorNavy,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun buildPlaceChips(place: Place): List<String> {
    val tagChips = place.tags
        .asSequence()
        .filterNot { it.equals("must_see", ignoreCase = true) }
        .map { it.prettyLabel() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)
        .toList()

    val fallback = listOfNotNull(
        place.category.prettyLabel(),
        place.zone?.prettyLabel()
    ).distinct()

    return (tagChips.ifEmpty { fallback }).take(4)
}

fun PlaceCategory.prettyLabel(): String = when (this) {
    PlaceCategory.EAT_AND_DRINK -> "Eat & Drink"
    PlaceCategory.SEE_AND_VISIT -> "See & Visit"
    PlaceCategory.ACTIVITIES -> "Activities"
    PlaceCategory.HIDDEN_GEMS -> "Hidden Gems"
    PlaceCategory.PRACTICAL -> "Practical"
}

fun String.prettyLabel(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        }

private fun BestTime.toUiLabel(): String = when (this) {
    BestTime.MORNING -> "Best in morning"
    BestTime.AFTERNOON -> "Best in afternoon"
    BestTime.EVENING -> "Best in evening"
    BestTime.ANYTIME -> "Any time"
}

fun Int.formatDuration(): String = when {
    this < 60 -> "${this} min"
    this % 60 == 0 -> "${this / 60} h"
    else -> "${this / 60} h ${this % 60} min"
}

private fun PriceIndicator.toFriendlyLabel(): String = when (this) {
    PriceIndicator.BUDGET -> "Free entry"
    PriceIndicator.MODERATE -> "Moderate"
    PriceIndicator.EXPENSIVE -> "Premium entry"
}
