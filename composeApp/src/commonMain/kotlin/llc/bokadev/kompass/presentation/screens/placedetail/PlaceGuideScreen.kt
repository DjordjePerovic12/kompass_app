package llc.bokadev.kompass.presentation.screens.placedetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.AudioGuidePlaybackState
import llc.bokadev.kompass.core.util.buildGuideMapHtml
import llc.bokadev.kompass.core.util.buildMapsUrl
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.shared.InlineHtmlMapView
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlaceGuideScreen(
    id: String,
    autoplay: Boolean = false,
    deep: Boolean = false,
    onBack: () -> Unit = {}
) {
    val vm: PlaceGuideViewModel = koinViewModel(parameters = { parametersOf(id, autoplay, deep) })
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(id) {
        analytics.trackScreenView("place_guide")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Unlocked place guide",
                title = if (deep) "KOMPASS Deep" else "Audio Guide",
                subtitle = if (deep) "A quieter companion layer while you explore" else "Listen while exploring on-site",
                showBack = true,
                onBackClick = {
                    vm.onIntent(PlaceGuideEvent.StopPlayback)
                    onBack()
                }
            )
        }
    ) {
        PlaceGuideScreenContent(
            state = state,
            deep = deep,
            onIntent = vm::onIntent,
            onAudioStarted = { place ->
                analytics.trackAudioPlay(place.id, place.cityId)
            }
        )
    }
}

@Composable
private fun PlaceGuideScreenContent(
    state: PlaceGuideState,
    deep: Boolean,
    onIntent: (PlaceGuideEvent) -> Unit,
    onAudioStarted: (Place) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val uriHandler = LocalUriHandler.current
    var mapVisible by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var appInBackground by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, state.playback.sourceUrl) {
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
            if (!appInBackground && state.playback.sourceUrl != null) {
                onIntent(PlaceGuideEvent.StopPlayback)
            }
        }
    }

    when {
        state.error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.error, color = colors.colorError)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { onIntent(PlaceGuideEvent.Retry) }) {
                    Text("Retry")
                }
            }
        }

        state.place != null -> {
            val place = state.place
            val zoneLabel = place.zone?.takeIf { it.isNotBlank() }?.prettyGuideLabel()
            val metaLinePrimary = buildString {
                append(place.category.prettyGuideCategoryLabel())
                zoneLabel?.let {
                    append(" · ")
                    append(it)
                }
            }
            val metaLineSecondary = buildString {
                append(place.bestTime.toGuideMomentLabel())
                place.estimatedDuration?.let {
                    append(" · ")
                    append(it.formatGuideDuration())
                    append(" walk")
                }
            }
            val mapQuery = buildString {
                append(place.localizedName(lang))
                zoneLabel?.let {
                    append(", ")
                    append(it)
                }
                append(" @")
                append(place.latitude)
                append(",")
                append(place.longitude)
            }
            val mapHtml = remember(place.id, state.currentLocation, lang) {
                buildGuideMapHtml(
                    placeName = place.localizedName(lang),
                    destination = llc.bokadev.kompass.domain.model.GeoPoint(place.latitude, place.longitude),
                    currentLocation = state.currentLocation
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.colorHomeCanvas)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
                ) {
                    PlacePhotoHeader(
                        imageUrls = place.photos.map(::buildPhotoUrl),
                        modifier = Modifier.height(320.dp),
                        imageAspectRatio = null
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-28).dp)
                            .background(
                                color = colors.colorWhite,
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            )
                            .padding(horizontal = 22.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = place.localizedName(lang),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 46.sp,
                                    lineHeight = 50.sp,
                                    letterSpacing = (-1).sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.colorNavy
                            )
                            Text(
                                text = metaLinePrimary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.colorSignalStrong.copy(alpha = 0.72f)
                            )
                            Text(
                                text = metaLineSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                color = colors.colorSlate.copy(alpha = 0.68f)
                            )
                        }

                        ListeningExperienceCard(
                            place = place,
                            playback = state.playback,
                            audioReady = state.audioUrl != null,
                            hasAudioAccess = state.hasAudioAccess,
                            onTogglePlayPause = {
                                if (!state.playback.isPlaying && state.audioUrl != null) {
                                    onAudioStarted(place)
                                }
                                onIntent(PlaceGuideEvent.TogglePlayPause)
                            },
                            onSeek = { onIntent(PlaceGuideEvent.SeekTo(it)) }
                        )

                        ExploreAroundCard(
                            placeName = place.localizedName(lang),
                            mapHtml = mapHtml,
                            mapVisible = mapVisible,
                            onToggleMap = { mapVisible = !mapVisible },
                            onOpenInMaps = { uriHandler.openUri(buildMapsUrl(mapQuery)) }
                        )

                        EditorialSection(
                            title = "Overview",
                            body = if (deep) {
                                place.localizedDeepText(lang).ifBlank { place.localizedDescription(lang) }
                            } else {
                                place.localizedDescription(lang)
                            }
                        )

                        if (!deep) {
                            place.localizedLongDescription(lang).takeIf(String::isNotBlank)?.let {
                                EditorialSection(
                                    title = "More Context",
                                    body = it
                                )
                            }
                        }

                        place.localizedLocalsTip(lang).takeIf(String::isNotBlank)?.let {
                            EditorialSection(
                                title = "Local Perspective",
                                body = it
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun ListeningExperienceCard(
    place: Place,
    playback: AudioGuidePlaybackState,
    audioReady: Boolean,
    hasAudioAccess: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val colors = KompassTheme.colors
    val durationMs = playback.durationMs.coerceAtLeast(0L)
    val progressMs = playback.progressMs.coerceIn(0L, durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE)
    val approximateMinutes = when {
        durationMs > 0L -> (durationMs / 60000L).coerceAtLeast(1L).toString() + " minutes"
        place.estimatedDuration != null -> "${(place.estimatedDuration / 2).coerceAtLeast(4)} minutes"
        else -> "12 minutes"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colors.colorSurfaceMid,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Audio Guide",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.colorNavy
            )
            Text(
                text = "Narrated walking guide with context you can listen to while moving through the site.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
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
                    .background(if (audioReady) colors.colorOrangeMain else colors.colorOrangeMain.copy(alpha = 0.32f))
                    .clickable(enabled = audioReady, onClick = onTogglePlayPause),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (playback.isPlaying) "❚❚" else "▶",
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
                        playback.isBuffering -> "Preparing playback"
                        !hasAudioAccess -> "Available with premium access"
                        audioReady -> "Ready to play"
                        else -> "Audio is still preparing"
                    }
                )
            }
        }

        if (durationMs > 0L || progressMs > 0L) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Slider(
                    value = progressMs.toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
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

        playback.error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = colors.colorError
            )
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

@Composable
private fun ExploreAroundCard(
    placeName: String,
    mapHtml: String,
    mapVisible: Boolean,
    onToggleMap: () -> Unit,
    onOpenInMaps: () -> Unit
) {
    val colors = KompassTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(colors.colorWhite)
                .border(1.dp, colors.colorSurfaceMid.copy(alpha = 0.75f), RoundedCornerShape(26.dp))
                .clickable(onClick = onToggleMap)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InlineHtmlMapView(
                html = mapHtml,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Explore Around",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.colorNavy
                )
                Text(
                    text = "Nearby sites, cafés, and viewpoints around $placeName.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 21.sp
                    ),
                    color = colors.colorSlate.copy(alpha = 0.78f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        AnimatedVisibility(
            visible = mapVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InlineHtmlMapView(
                    html = mapHtml,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                TextButton(
                    onClick = onOpenInMaps,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Open in Maps app →",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.colorOrangeMain
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorialSection(
    title: String,
    body: String
) {
    val colors = KompassTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 31.sp
            ),
            color = colors.colorSlate.copy(alpha = 0.82f)
        )
    }
}

@Composable
private fun PremiumDeepDiveCard() {
    val colors = KompassTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.colorSurface, RoundedCornerShape(24.dp))
            .border(1.dp, colors.colorSurfaceMid.copy(alpha = 0.75f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Historical Layers",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = "Unlock the deeper cultural context, architectural notes, and quieter details behind the facade.",
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = colors.colorSlate.copy(alpha = 0.78f)
        )
        Text(
            text = "Premium",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.4.sp,
                fontWeight = FontWeight.Medium
            ),
            color = colors.colorOrangeMain
        )
    }
}

private fun Long.toClockLabel(): String {
    val totalSeconds = (this / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private fun String.prettyGuideLabel(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        }

private fun PlaceCategory.prettyGuideCategoryLabel(): String = when (this) {
    PlaceCategory.EAT_AND_DRINK -> "Eat & Drink"
    PlaceCategory.SEE_AND_VISIT -> "See & Visit"
    PlaceCategory.ACTIVITIES -> "Activities"
    PlaceCategory.HIDDEN_GEMS -> "Hidden Gems"
    PlaceCategory.PRACTICAL -> "Practical"
}

private fun BestTime.toGuideMomentLabel(): String = when (this) {
    BestTime.MORNING -> "Best in morning"
    BestTime.AFTERNOON -> "Best in afternoon"
    BestTime.EVENING -> "Best in evening"
    BestTime.ANYTIME -> "Best any time"
}

private fun Int.formatGuideDuration(): String = when {
    this < 60 -> "${this} min"
    this % 60 == 0 -> "${this / 60} h"
    else -> "${this / 60} h ${this % 60} min"
}
