package llc.bokadev.kompass.presentation.screens.experiencedetail

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import llc.bokadev.kompass.core.util.AudioGuidePlaybackState
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.presentation.screens.placedetail.components.InfoChip
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.shared.DetailNativeMapCard
import llc.bokadev.kompass.presentation.shared.FavoriteToggleButton
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExperienceDetailScreen(
    id: String,
    onBack: () -> Unit = {},
    onLearnMore: () -> Unit = {}
) {
    val vm: ExperienceDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val freeGuideVm: ExperienceGuideViewModel = koinViewModel(
        key = "experience-guide-free-$id",
        parameters = { parametersOf(id, false, false) }
    )
    val deepGuideVm: ExperienceGuideViewModel = koinViewModel(
        key = "experience-guide-deep-$id",
        parameters = { parametersOf(id, false, true) }
    )
    val state by vm.state.collectAsState()
    val freeGuideState by freeGuideVm.state.collectAsState()
    val deepGuideState by deepGuideVm.state.collectAsState()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val favorites by favoritesRepository.favoritesFlow.collectAsState()

    ExperienceDetailScreenContent(
        state = state,
        isFavorited = favorites.any { it.type == FavoriteItemType.ACTIVITY && it.id == id },
        onFavoriteClick = { favoritesRepository.toggleFavorite(FavoriteItemType.ACTIVITY, id) },
        onIntent = vm::onIntent,
        onBack = onBack,
        onLearnMore = onLearnMore,
        freeGuideState = freeGuideState,
        onFreeGuideIntent = freeGuideVm::onIntent,
        deepGuideState = deepGuideState,
        onDeepGuideIntent = deepGuideVm::onIntent
    )
}

@Composable
private fun ExperienceDetailScreenContent(
    state: ExperienceDetailState,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    onIntent: (ExperienceDetailEvent) -> Unit,
    onBack: () -> Unit,
    onLearnMore: () -> Unit,
    freeGuideState: ExperienceGuideState,
    onFreeGuideIntent: (ExperienceGuideEvent) -> Unit,
    deepGuideState: ExperienceGuideState,
    onDeepGuideIntent: (ExperienceGuideEvent) -> Unit
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
                        .clickable { onIntent(ExperienceDetailEvent.Retry) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorNavy
                )
            }

            state.activity != null -> {
                ExperienceDetailBody(
                    activity = state.activity,
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
private fun ExperienceDetailBody(
    activity: Experience,
    hasAudioAccess: Boolean,
    hasDetailAccess: Boolean,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    onBack: () -> Unit,
    onLearnMore: () -> Unit,
    freeGuideState: ExperienceGuideState,
    onFreeGuideIntent: (ExperienceGuideEvent) -> Unit,
    deepGuideState: ExperienceGuideState,
    onDeepGuideIntent: (ExperienceGuideEvent) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val activityName = activity.localizedName(lang)
    val locationLabel = activity.localizedLocation(lang).takeIf(String::isNotBlank)
    val metaLine = listOfNotNull(
        activity.category?.takeIf(String::isNotBlank)?.prettyActivityCategory(),
        locationLabel
    ).joinToString(" · ")

    val practicalLine = listOfNotNull(
        activity.bestTime.toUiLabel().takeIf { it != "Any time" },
        activity.durationMin?.formatDuration()
    ).joinToString(" · ")

    val chips = listOfNotNull(
        activity.category?.takeIf(String::isNotBlank)?.prettyActivityCategory(),
        activity.durationMin?.formatDuration(),
        activity.bestTime.toUiLabel().takeIf { it != "Any time" },
        locationLabel
    ).distinct().take(4)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                PlacePhotoHeader(
                    imageUrl = activity.photos.firstOrNull()?.let { buildPhotoUrl(it) },
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
                        text = activityName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 31.sp,
                            lineHeight = 35.sp,
                            letterSpacing = (-0.5).sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.colorNavy
                    )
                    if (metaLine.isNotBlank()) {
                        Text(
                            text = metaLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.colorNavy.copy(alpha = 0.62f)
                        )
                    }
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
                            InfoChip(label = chip, amber = index == 0)
                        }
                    }
                }

                if (activity.latitude != null && activity.longitude != null) {
                    DetailNativeMapCard(
                        placeName = activityName,
                        summary = "See where this experience sits and orient yourself before heading out.",
                        destination = GeoPoint(activity.latitude, activity.longitude)
                    )
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))

                DetailSection(
                    title = "Description",
                    body = activity.localizedDescription(lang)
                )

                activity.localizedLongDescription(lang)
                    .takeIf { it.isNotBlank() }
                    ?.let { longText ->
                        DetailSection(
                            title = "More Context",
                            body = longText
                        )
                    }

                activity.localizedHowToGetThere(lang)
                    .takeIf { it.isNotBlank() }
                    ?.let { howToGetThere ->
                        DetailSection(
                            title = "How to get there",
                            body = howToGetThere
                        )
                    }

                if (activity.audioFile != null) {
                    InlineActivityAudioGuideSection(
                        activity = activity,
                        guideState = freeGuideState,
                        hasAudioAccess = hasAudioAccess,
                        onIntent = onFreeGuideIntent
                    )
                }

                if (activity.hasDeepContent(lang)) {
                    val deepBody = activity.localizedDeepText(lang)
                    if (hasDetailAccess) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            DeepCompanionSection(
                                title = "KOMPASS Deep",
                                body = deepBody.ifBlank {
                                    "A quieter companion layer for this experience with added atmosphere, context, and guided awareness."
                                },
                                ctaLabel = null,
                                isLocked = false,
                                onPrimaryAction = null
                            )
                            if (!activity.deepAudioFile.isNullOrEmpty()) {
                                InlineActivityAudioGuideSection(
                                    activity = activity,
                                    guideState = deepGuideState,
                                    hasAudioAccess = hasDetailAccess,
                                    onIntent = onDeepGuideIntent,
                                    title = "Deep Audio",
                                    description = "A quieter companion layer with additional route context, atmosphere, and guided awareness.",
                                    availabilityText = "Deep audio ready"
                                )
                            }
                        }
                    } else {
                        DeepCompanionSection(
                            title = "KOMPASS Deep",
                            body = "Beyond routes and viewpoints, Deep adds short contextual moments and a more immersive way to experience Kotor for those who want it.",
                            ctaLabel = "What is Deep?",
                            isLocked = true,
                            onPrimaryAction = onLearnMore
                        )
                    }
                }

                activity.externalWebsite
                    ?.takeIf(String::isNotBlank)
                    ?.let { link ->
                        DetailFactCard(
                            label = "Website",
                            value = link
                        )
                    }
            }

            Spacer(modifier = Modifier.height(34.dp))
        }
    }
}

@Composable
private fun InlineActivityAudioGuideSection(
    activity: Experience,
    guideState: ExperienceGuideState,
    hasAudioAccess: Boolean,
    onIntent: (ExperienceGuideEvent) -> Unit,
    title: String = "Audio Guide",
    description: String = "A short guide you can listen to while moving through this experience.",
    availabilityText: String = "Ready to play"
) {
    StopActivityPlaybackWhenLeavingForeground(
        playbackSourceUrl = guideState.playback.sourceUrl,
        onStopPlayback = { onIntent(ExperienceGuideEvent.StopPlayback) }
    )

    val playback = guideState.playback
    val isCurrentSource = guideState.audioUrl != null && playback.sourceUrl == guideState.audioUrl
    val durationMs = if (isCurrentSource) playback.durationMs.coerceAtLeast(0L) else 0L
    val progressMs = if (isCurrentSource) playback.progressMs.coerceIn(0L, durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE) else 0L
    val approximateMinutes = when {
        durationMs > 0L -> "${(durationMs / 60000L).coerceAtLeast(1L)} minutes"
        activity.durationMin != null -> "${(activity.durationMin / 2).coerceAtLeast(4)} minutes"
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
                        onIntent(ExperienceGuideEvent.TogglePlayPause)
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
                ActivityGuideMetaLine("Approx. $approximateMinutes")
                ActivityGuideMetaLine("Best experienced on-site")
                ActivityGuideMetaLine(
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
                    onValueChange = { onIntent(ExperienceGuideEvent.SeekTo(it.toLong())) },
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
                        text = progressMs.toActivityClockLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.colorSlate.copy(alpha = 0.7f)
                    )
                    Text(
                        text = durationMs.toActivityClockLabel(),
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
private fun StopActivityPlaybackWhenLeavingForeground(
    playbackSourceUrl: String?,
    onStopPlayback: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var appInBackground by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, playbackSourceUrl) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> appInBackground = true
                Lifecycle.Event.ON_START -> appInBackground = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (!appInBackground && playbackSourceUrl != null) {
                onStopPlayback()
            }
        }
    }
}

@Composable
private fun ActivityGuideMetaLine(text: String) {
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

private fun Long.toActivityClockLabel(): String {
    val totalSeconds = (this / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
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
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = colors.colorNavy.copy(alpha = 0.76f)
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

private fun String.prettyActivityCategory(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

private fun BestTime.toUiLabel(): String = when (this) {
    BestTime.MORNING -> "Best in morning"
    BestTime.AFTERNOON -> "Best in afternoon"
    BestTime.EVENING -> "Best in evening"
    BestTime.ANYTIME -> "Any time"
}

private fun Int.formatDuration(): String = when {
    this < 60 -> "${this} min"
    this % 60 == 0 -> "${this / 60} h"
    else -> "${this / 60} h ${this % 60} min"
}
