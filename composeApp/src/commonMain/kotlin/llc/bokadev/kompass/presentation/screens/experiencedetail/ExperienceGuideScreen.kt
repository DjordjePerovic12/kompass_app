package llc.bokadev.kompass.presentation.screens.experiencedetail

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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
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
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.screens.placedetail.components.PlacePhotoHeader
import llc.bokadev.kompass.presentation.shared.InlineHtmlMapView
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExperienceGuideScreen(
    id: String,
    autoplay: Boolean = false,
    deep: Boolean = false,
    onBack: () -> Unit = {}
) {
    val vm: ExperienceGuideViewModel = koinViewModel(parameters = { parametersOf(id, autoplay, deep) })
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(id) {
        analytics.trackScreenView("experience_guide")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "",
                title = if (deep) "KOMPASS Deep" else "Audio Guide",
                subtitle = if (deep) "A quieter companion layer while you explore" else "Listen while exploring on-site",
                showBack = true,
                onBackClick = {
                    vm.onIntent(ExperienceGuideEvent.StopPlayback)
                    onBack()
                }
            )
        }
    ) {
        ExperienceGuideScreenContent(
            state = state,
            deep = deep,
            onIntent = vm::onIntent,
            onAudioStarted = { activity ->
                analytics.trackGuideOpen(activity.id, activity.cityId)
                analytics.trackAudioPlay(activity.id, activity.cityId)
            }
        )
    }
}

@Composable
private fun ExperienceGuideScreenContent(
    state: ExperienceGuideState,
    deep: Boolean,
    onIntent: (ExperienceGuideEvent) -> Unit,
    onAudioStarted: (Experience) -> Unit
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
                onIntent(ExperienceGuideEvent.StopPlayback)
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
                Button(onClick = { onIntent(ExperienceGuideEvent.Retry) }) {
                    Text("Retry")
                }
            }
        }

        state.activity != null -> {
            val activity = state.activity
            val locationLabel = activity.localizedLocation(lang).takeIf { it.isNotBlank() }
            val metaLinePrimary = buildString {
                append(activity.category.prettyActivityCategory())
                locationLabel?.let {
                    append(" · ")
                    append(it)
                }
            }
            val metaLineSecondary = buildString {
                append(activity.bestTime.toGuideMomentLabel())
                activity.durationMin?.let {
                    append(" · ")
                    append(it.formatGuideDuration())
                }
            }
            val mapHtml = remember(activity.id, state.currentLocation, lang) {
                buildGuideMapHtml(
                    placeName = activity.localizedName(lang),
                    destination = llc.bokadev.kompass.domain.model.GeoPoint(
                        activity.latitude ?: 0.0,
                        activity.longitude ?: 0.0
                    ),
                    currentLocation = state.currentLocation
                )
            }
            val mapQuery = buildString {
                append(activity.localizedName(lang))
                locationLabel?.let {
                    append(", ")
                    append(it)
                }
                activity.latitude?.let { lat ->
                    activity.longitude?.let { lon ->
                        append(" @")
                        append(lat)
                        append(",")
                        append(lon)
                    }
                }
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
                        imageUrls = activity.photos.map(::buildPhotoUrl),
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
                                text = activity.localizedName(lang),
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

                        ListeningActivityCard(
                            activity = activity,
                            playback = state.playback,
                            audioReady = state.audioUrl != null,
                            hasAudioAccess = state.hasAudioAccess,
                            onTogglePlayPause = {
                                if (!state.playback.isPlaying && state.audioUrl != null) {
                                    onAudioStarted(activity)
                                }
                                onIntent(ExperienceGuideEvent.TogglePlayPause)
                            },
                            onSeek = { onIntent(ExperienceGuideEvent.SeekTo(it)) }
                        )

                        if (activity.latitude != null && activity.longitude != null) {
                            ExploreActivityAroundCard(
                                activityName = activity.localizedName(lang),
                                mapHtml = mapHtml,
                                mapVisible = mapVisible,
                                onToggleMap = { mapVisible = !mapVisible },
                                onOpenInMaps = { uriHandler.openUri(buildMapsUrl(mapQuery)) }
                            )
                        }

                        EditorialActivitySection(
                            title = "Overview",
                            body = if (deep) {
                                activity.localizedDeepText(lang).ifBlank { activity.localizedDescription(lang) }
                            } else {
                                activity.localizedDescription(lang)
                            }
                        )

                        if (!deep) {
                            activity.localizedLongDescription(lang).takeIf(String::isNotBlank)?.let {
                                EditorialActivitySection(
                                    title = "More Context",
                                    body = it
                                )
                            }
                        }

                        activity.localizedHowToGetThere(lang).takeIf(String::isNotBlank)?.let {
                            EditorialActivitySection(
                                title = "How to get there",
                                body = it
                            )
                        }

                        locationLabel?.let {
                            EditorialActivitySection(
                                title = "Local context",
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
private fun ListeningActivityCard(
    activity: Experience,
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
        activity.durationMin != null -> "${(activity.durationMin / 2).coerceAtLeast(4)} minutes"
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
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.colorNavy
            )
            Text(
                text = "Narrated route guide with context you can listen to while moving through the experience.",
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
                ActivityGuideMetaLine("Approx. $approximateMinutes")
                ActivityGuideMetaLine("Best experienced on-site")
                ActivityGuideMetaLine(
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

@Composable
private fun ExploreActivityAroundCard(
    activityName: String,
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

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Explore Around",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.colorNavy
                )
                Text(
                    text = "Nearby viewpoints, cafés, and useful stops around $activityName.",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
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
private fun EditorialActivitySection(
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
                lineHeight = 33.sp
            ),
            color = colors.colorSlate
        )
    }
}

@Composable
private fun ActivityPremiumDeepDiveCard() {
    val colors = KompassTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colors.colorSurfaceMid.copy(alpha = 0.82f),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = colors.colorOrangeMain.copy(alpha = 0.12f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Historical Layers",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = "Deeper editorial notes unlock with premium access once you want fuller context beyond the core guide.",
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = colors.colorSlate.copy(alpha = 0.82f)
        )
    }
}

private fun String?.prettyActivityCategory(): String =
    this?.split('_', '-', ' ')
        ?.filter { it.isNotBlank() }
        ?.joinToString(" ") { token ->
            token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        ?.takeIf { it.isNotBlank() }
        ?: "Activity"

private fun BestTime.toGuideMomentLabel(): String = when (this) {
    BestTime.MORNING -> "Best in morning"
    BestTime.AFTERNOON -> "Best in afternoon"
    BestTime.EVENING -> "Best in evening"
    BestTime.ANYTIME -> "Any time"
}

private fun Int.formatGuideDuration(): String = when {
    this < 60 -> "$this min"
    this % 60 == 0 -> "${this / 60} hr"
    else -> "${this / 60} hr ${this % 60} min"
}

private fun Long.toActivityClockLabel(): String {
    val totalSeconds = (this / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
