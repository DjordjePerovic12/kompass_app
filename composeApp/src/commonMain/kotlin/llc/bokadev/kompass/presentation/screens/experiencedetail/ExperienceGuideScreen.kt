package llc.bokadev.kompass.presentation.screens.experiencedetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.core.util.AudioGuidePlaybackState
import llc.bokadev.kompass.core.util.buildGuideMapHtml
import llc.bokadev.kompass.core.util.buildMapsUrl
import llc.bokadev.kompass.core.util.buildPhotoUrl
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.permissions.NotificationPermissionEffect
import llc.bokadev.kompass.presentation.permissions.rememberShouldShowNotificationPrompt
import llc.bokadev.kompass.presentation.screens.placedetail.components.InfoChip
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
    onBack: () -> Unit = {}
) {
    val vm: ExperienceGuideViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(id) {
        analytics.trackScreenView("guide")
    }

    LaunchedEffect(state.activity?.id) {
        state.activity?.let { analytics.trackGuideOpen(it.id, it.cityId) }
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Unlocked audio guide",
                title = "Guide",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        ExperienceGuideScreenContent(
            state = state,
            onIntent = vm::onIntent,
            onAudioStarted = { activity ->
                analytics.trackAudioPlay(activity.id, activity.cityId)
            }
        )
    }
}

@Composable
private fun ExperienceGuideScreenContent(
    state: ExperienceGuideState,
    onIntent: (ExperienceGuideEvent) -> Unit,
    onAudioStarted: (Experience) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val uriHandler = LocalUriHandler.current
    var mapVisible by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var shouldRequestPermission by remember { mutableStateOf(false) }
    val shouldShowNotificationPrompt = rememberShouldShowNotificationPrompt()

    LaunchedEffect(state.audioUrl) {
        if (state.audioUrl != null && shouldShowNotificationPrompt) {
            showNotificationDialog = true
        }
    }

    NotificationPermissionEffect(
        enabled = shouldRequestPermission,
        onPermissionResult = { shouldRequestPermission = false }
    )

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

            if (showNotificationDialog) {
                AlertDialog(
                    onDismissRequest = { showNotificationDialog = false },
                    title = { Text("Keep audio visible in background") },
                    text = { Text("Allow notifications so KOmpass can show playback controls while the guide continues playing in the background on Android.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showNotificationDialog = false
                            shouldRequestPermission = true
                        }) { Text("Continue") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNotificationDialog = false }) { Text("Not now") }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(colors.colorSurface)
            ) {
                PlacePhotoHeader(
                    imageUrl = activity.photos.firstOrNull()?.let { buildPhotoUrl(it) }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.colorWhite, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = activity.localizedName(lang),
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.colorNavy
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        activity.category?.takeIf(String::isNotBlank)
                            ?.let { InfoChip(it.prettyActivityCategory()) }
                        activity.durationMin?.let { InfoChip("$it min") }
                        InfoChip(activity.bestTime.toUiLabel())
                    }

                    if (activity.latitude != null && activity.longitude != null) {
                        val destination = GeoPoint(activity.latitude, activity.longitude)
                        val mapHtml = remember(destination, state.currentLocation) {
                            buildGuideMapHtml(
                                placeName = activity.localizedName(lang),
                                destination = destination,
                                currentLocation = state.currentLocation
                            )
                        }
                        val mapsQuery = remember(destination) {
                            buildString {
                                append(activity.localizedName(lang))
                                if (activity.localizedLocation(lang).isNotBlank()) {
                                    append(", ")
                                    append(activity.localizedLocation(lang))
                                }
                                append(" @")
                                append(destination.latitude)
                                append(",")
                                append(destination.longitude)
                            }
                        }

                        OutlinedButton(
                            onClick = { mapVisible = !mapVisible },
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(if (mapVisible) "HIDE MAP" else "SHOW MAP")
                        }

                        // Use fade-only: expandVertically clamps height during
                        // animation which races with the WebView's layout pass,
                        // causing window.innerWidth/Height to read 0 in JS.
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
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                TextButton(
                                    onClick = { uriHandler.openUri(buildMapsUrl(mapsQuery)) },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        text = "Open in Maps app →",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.colorAmberDark
                                    )
                                }
                            }
                        }
                    }

                    GuideDetailSection("Overview", activity.localizedDescription(lang))

                    activity.localizedLongDescription(lang).takeIf(String::isNotBlank)?.let {
                        GuideDetailSection("Story", it)
                    }

                    activity.localizedLocation(lang).takeIf(String::isNotBlank)?.let {
                        GuideDetailSection("Location", it)
                    }

                    activity.localizedHowToGetThere(lang).takeIf(String::isNotBlank)?.let {
                        GuideDetailSection("How to get there", it)
                    }

                    AudioGuideCard(
                        activity = activity,
                        playback = state.playback,
                        audioReady = state.audioUrl != null,
                        onTogglePlayPause = {
                            if (!state.playback.isPlaying && state.audioUrl != null) {
                                onAudioStarted(activity)
                            }
                            onIntent(ExperienceGuideEvent.TogglePlayPause)
                        },
                        onSeek = { onIntent(ExperienceGuideEvent.SeekTo(it)) }
                    )

                    activity.externalWebsite?.takeIf(String::isNotBlank)?.let {
                        GuideDetailSection("External link", it)
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioGuideCard(
    activity: Experience,
    playback: AudioGuidePlaybackState,
    audioReady: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val colors = KompassTheme.colors
    val durationMs = playback.durationMs.coerceAtLeast(0L)
    val progressMs = playback.progressMs.coerceIn(0L, durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colors.colorNavy, colors.colorNavyMedium)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Audio Guide",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "${progressMs.toClockLabel()} / ${durationMs.toClockLabel()}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f)
            )
        }

        Spacer(Modifier.height(6.dp))

        // Seek slider
        Slider(
            value = progressMs.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
            enabled = audioReady && durationMs > 0L,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = colors.colorAmber,
                activeTrackColor = colors.colorAmber,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                disabledThumbColor = Color.White.copy(alpha = 0.25f),
                disabledActiveTrackColor = Color.White.copy(alpha = 0.2f),
                disabledInactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )

        Spacer(Modifier.height(8.dp))

        // Play / Pause button
        Box(
            modifier = Modifier
                .size(62.dp)
                .background(
                    color = if (audioReady) colors.colorAmber else Color.White.copy(alpha = 0.12f),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable(enabled = audioReady) { onTogglePlayPause() },
            contentAlignment = Alignment.Center
        ) {
            when {
                playback.isBuffering -> CircularProgressIndicator(
                    color = colors.colorNavy,
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 2.5.dp
                )
                playback.isPlaying -> Text(
                    text = "⏸",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.colorNavy
                )
                else -> Text(
                    text = "▶",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (audioReady) colors.colorNavy else Color.White.copy(alpha = 0.35f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Status text / error
        if (playback.error != null) {
            Text(
                text = playback.error,
                style = MaterialTheme.typography.bodySmall,
                color = colors.colorError,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = when {
                    !audioReady -> "Audio not yet available"
                    playback.isPlaying -> "Continues playing in background"
                    else -> "Tap to start the audio guide"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GuideDetailSection(title: String, body: String) {
    val colors = KompassTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.colorNavy
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.colorSlate
        )
    }
}

private fun Long.toClockLabel(): String {
    val totalSeconds = (this / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

private fun String.prettyActivityCategory(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

private fun llc.bokadev.kompass.domain.model.BestTime.toUiLabel(): String = when (this) {
    llc.bokadev.kompass.domain.model.BestTime.MORNING -> "Morning"
    llc.bokadev.kompass.domain.model.BestTime.AFTERNOON -> "Afternoon"
    llc.bokadev.kompass.domain.model.BestTime.EVENING -> "Evening"
    llc.bokadev.kompass.domain.model.BestTime.ANYTIME -> "Any time"
}
