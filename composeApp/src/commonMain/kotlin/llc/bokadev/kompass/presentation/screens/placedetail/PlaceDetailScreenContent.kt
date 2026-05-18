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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.util.AudioGuidePlaybackState
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.model.PriceIndicator
import llc.bokadev.kompass.presentation.screens.placedetail.components.InfoChip
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.shared.DetailNativeMapCard
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
    freeGuideState: PlaceGuideState,
    onFreeGuideIntent: (PlaceGuideEvent) -> Unit,
    deepGuideState: PlaceGuideState,
    onDeepGuideIntent: (PlaceGuideEvent) -> Unit
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
                    freeGuideState = freeGuideState,
                    onFreeGuideIntent = onFreeGuideIntent,
                    deepGuideState = deepGuideState,
                    onDeepGuideIntent = onDeepGuideIntent
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
    freeGuideState: PlaceGuideState,
    onFreeGuideIntent: (PlaceGuideEvent) -> Unit,
    deepGuideState: PlaceGuideState,
    onDeepGuideIntent: (PlaceGuideEvent) -> Unit
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

                DetailNativeMapCard(
                    placeName = place.localizedName(lang),
                    summary = "Nearby sites, cafés, and viewpoints around ${place.localizedName(lang)}.",
                    destination = GeoPoint(place.latitude, place.longitude)
                )

                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))

                DetailSection(
                    title = "Description",
                    body = place.localizedDescription(lang)
                )

                place.localizedLongDescription(lang)
                    .takeIf { it.isNotBlank() }
                    ?.let { longText ->
                        DetailSection(
                            title = "More Context",
                            body = longText
                        )
                    }

                place.localizedLocalsTip(lang)
                    .takeIf { it.isNotBlank() }
                    ?.let { tip ->
                        DetailFactCard(
                            label = "Local perspective",
                            value = tip
                        )
                    }

                if (place.audioFile != null) {
                    InlinePlaceAudioGuideSection(
                        place = place,
                        guideState = freeGuideState,
                        hasAudioAccess = hasAudioAccess,
                        onIntent = onFreeGuideIntent
                    )
                }

                if (place.hasDeepContent(lang)) {
                    val deepBody = place.localizedDeepText(lang)
                    if (hasDetailAccess) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DeepCompanionSection(
                                title = "KOMPASS Deep",
                                body = deepBody.ifBlank {
                                    "A quieter companion layer for this place with extra atmosphere, context, and guided awareness."
                                },
                                ctaLabel = null,
                                isLocked = false,
                                onPrimaryAction = null
                            )
                            if (!place.deepAudioFile.isNullOrEmpty()) {
                                InlinePlaceAudioGuideSection(
                                    place = place,
                                    guideState = deepGuideState,
                                    hasAudioAccess = hasDetailAccess,
                                    onIntent = onDeepGuideIntent,
                                    title = "Deep Audio",
                                    description = "A quieter companion layer with additional context, atmosphere, and spatial guidance.",
                                    availabilityText = "Deep audio ready"
                                )
                            }
                        }
                    } else {
                        DeepCompanionSection(
                            title = "KOMPASS Deep",
                            body = "Beyond landmarks, routes, and viewpoints, Deep adds a quieter companion layer with short contextual moments across selected places and experiences in Kotor.",
                            ctaLabel = "What is Deep?",
                            isLocked = true,
                            onPrimaryAction = onLearnMore
                        )
                    }
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
private fun InlinePlaceAudioGuideSection(
    place: Place,
    guideState: PlaceGuideState,
    hasAudioAccess: Boolean,
    onIntent: (PlaceGuideEvent) -> Unit,
    title: String = "Audio Guide",
    description: String = "A short guide you can listen to while moving through the site.",
    availabilityText: String = "Ready to play"
) {
    StopPlaybackWhenLeavingForeground(
        playbackSourceUrl = guideState.playback.sourceUrl,
        onStopPlayback = { onIntent(PlaceGuideEvent.StopPlayback) }
    )

    val playback = guideState.playback
    val isCurrentSource = guideState.audioUrl != null && playback.sourceUrl == guideState.audioUrl
    val durationMs = if (isCurrentSource) playback.durationMs.coerceAtLeast(0L) else 0L
    val progressMs = if (isCurrentSource) playback.progressMs.coerceIn(0L, durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE) else 0L
    val approximateMinutes = when {
        durationMs > 0L -> "${(durationMs / 60000L).coerceAtLeast(1L)} minutes"
        place.estimatedDuration != null -> "${(place.estimatedDuration / 2).coerceAtLeast(4)} minutes"
        else -> "12 minutes"
    }
    val colors = KompassTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.colorSurfaceMid, shape = RoundedCornerShape(28.dp))
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = colors.colorSlate.copy(alpha = 0.8f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(if (guideState.audioUrl != null && hasAudioAccess) colors.colorOrangeMain else colors.colorOrangeMain.copy(alpha = 0.32f))
                    .clickable(enabled = guideState.audioUrl != null && hasAudioAccess) {
                        onIntent(PlaceGuideEvent.TogglePlayPause)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCurrentSource && playback.isPlaying) "❚❚" else "▶",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.colorWhite
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GuideMetaLine("Approx. $approximateMinutes")
                GuideMetaLine("Best experienced on-site")
                GuideMetaLine(
                    when {
                        isCurrentSource && playback.isBuffering -> "Preparing playback"
                        !hasAudioAccess -> "Available with KOMPASS Deep"
                        guideState.audioUrl != null -> availabilityText
                        else -> "Audio is still preparing"
                    }
                )
            }
        }

        if (durationMs > 0L || progressMs > 0L) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Slider(
                    value = progressMs.toFloat(),
                    onValueChange = { onIntent(PlaceGuideEvent.SeekTo(it.toLong())) },
                    valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.colorOrangeMain,
                        activeTrackColor = colors.colorOrangeMain,
                        inactiveTrackColor = colors.colorWhite.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier.height(18.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = progressMs.toClockLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.colorSlate.copy(alpha = 0.7f)
                    )
                    Text(
                        text = durationMs.toClockLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.colorSlate.copy(alpha = 0.7f)
                    )
                }
            }
        }

        guideState.playback.error?.takeIf { isCurrentSource }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = colors.colorError
            )
        }
    }
}

@Composable
private fun StopPlaybackWhenLeavingForeground(
    playbackSourceUrl: String?,
    onStopPlayback: () -> Unit
) {
    DisposableEffect(playbackSourceUrl) {
        onDispose {
            if (playbackSourceUrl != null) {
                onStopPlayback()
            }
        }
    }
}

@Composable
private fun GuideMetaLine(text: String) {
    val colors = KompassTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        color = colors.colorSlate.copy(alpha = 0.76f)
    )
}

private fun Long.toClockLabel(): String {
    val totalSeconds = (this / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(2, '0')}"
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
private fun DeepCompanionSection(
    title: String,
    body: String,
    isLocked: Boolean,
    ctaLabel: String?,
    onPrimaryAction: (() -> Unit)?
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
        if (ctaLabel != null && onPrimaryAction != null) {
            Text(
                text = ctaLabel,
                modifier = Modifier.clickable(onClick = onPrimaryAction),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorOrangeMain
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
