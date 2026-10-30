package llc.bokadev.kompass.presentation.screens.listen

import llc.bokadev.kompass.domain.model.AudioCatalogEntry
import llc.bokadev.kompass.domain.model.AudioSource
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.audio.AudioState
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.ListenStrings
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.toClockDuration
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.presentation.screens.categorylist.areaLabel
import llc.bokadev.kompass.presentation.screens.home.components.ChevronLeftIcon
import llc.bokadev.kompass.presentation.screens.home.components.HeadphonesIcon
import llc.bokadev.kompass.presentation.screens.listen.components.DiscState
import llc.bokadev.kompass.presentation.screens.listen.components.ListenTrackRow
import llc.bokadev.kompass.presentation.screens.listen.components.NowPlayingCard
import llc.bokadev.kompass.presentation.screens.listen.components.NowPlayingLabels
import llc.bokadev.kompass.presentation.shared.LamplightSegmentedControl
import llc.bokadev.kompass.presentation.shared.SegmentOption
import llc.bokadev.kompass.presentation.theme.HomePalette
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import llc.bokadev.kompass.presentation.theme.pageGradientBackground

private data class ListenActions(
    val onBack: () -> Unit,
    val onView: (ListenView) -> Unit,
    val onPlay: (ListenTrack) -> Unit,
    val onOpen: (ListenTrack) -> Unit,
    val onDownload: (ListenTrack) -> Unit,
    val onConfirmRemove: (ListenTrack) -> Unit,
    val onDismissRemove: () -> Unit,
    val onToggleCurrent: () -> Unit,
    val onStop: () -> Unit,
    val onSeek: (Long) -> Unit,
    val onSkip: (Long) -> Unit,
    val onOpenCurrent: () -> Unit,
    val onRetry: () -> Unit
)

/**
 * Lamplight audio library ("Listen"): play in place, one active track, a Now playing card while
 * something is loaded, and honest Downloaded / Streams online / Needs internet labels.
 */
@Composable
fun ListenScreenContent(
    state: ListenState,
    /** What the Now playing card shows: the live state, or the last one for 1s after a track ends. */
    nowPlaying: AudioState?,
    audio: AudioState,
    online: Boolean,
    /** Durations learned from the player this session, by track id (the CMS has none). */
    knownDurations: Map<String, Long>,
    removing: ListenTrack?,
    reduceMotion: Boolean,
    strings: AppStrings,
    lang: String,
    onAction: (ListenAction) -> Unit
) {
    val actions = ListenActions(
        onBack = { onAction(ListenAction.Back) },
        onView = { onAction(ListenAction.SelectView(it)) },
        onPlay = { onAction(ListenAction.Play(it)) },
        onOpen = { onAction(ListenAction.Open(it.entry.source, it.entry.id)) },
        onDownload = { onAction(ListenAction.Download(it)) },
        onConfirmRemove = { onAction(ListenAction.ConfirmRemove(it)) },
        onDismissRemove = { onAction(ListenAction.DismissRemove) },
        onToggleCurrent = { onAction(ListenAction.ToggleCurrent) },
        onStop = { onAction(ListenAction.Stop) },
        onSeek = { onAction(ListenAction.Seek(it)) },
        onSkip = { onAction(ListenAction.Skip(it)) },
        onOpenCurrent = { onAction(ListenAction.OpenCurrent) },
        onRetry = { onAction(ListenAction.Retry) }
    )
    val l = strings.listen
    LazyColumn(
        modifier = Modifier.fillMaxSize().pageGradientBackground(),
        contentPadding = PaddingValues(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "top") { TopBar(state.view, l, strings.detail.back, actions) }
        nowPlaying?.trackOrNull?.let { track ->
            item(key = "now") {
                val s = nowPlaying
                val disc = s.disc()
                val position = (s as? AudioState.Playing)?.positionMs ?: (s as? AudioState.Paused)?.positionMs ?: 0L
                val duration = (s as? AudioState.Playing)?.durationMs ?: (s as? AudioState.Paused)?.durationMs ?: knownDurations[track.id]
                NowPlayingCard(
                    title = track.title,
                    disc = disc,
                    positionMs = position,
                    durationMs = duration,
                    labels = NowPlayingLabels(
                        status = when (disc) {
                            DiscState.Loading -> l.loading
                            DiscState.Playing -> l.nowPlaying
                            DiscState.Failed -> l.couldntPlay
                            else -> l.paused
                        },
                        stop = l.stop, back15 = l.back15, forward15 = l.forward15,
                        mainButton = playDescription(disc, track.title, position, duration, l),
                        region = l.nowPlayingRegion, progressValue = l.progressValue, retry = l.retry
                    ),
                    reduceMotion = reduceMotion,
                    formatTime = { it.toClockDuration() ?: "0:00" },
                    onToggle = actions.onToggleCurrent,
                    onStop = actions.onStop,
                    onSeek = actions.onSeek,
                    onSkip = actions.onSkip,
                    onOpen = actions.onOpenCurrent,
                    modifier = Modifier
                        .animateItem(fadeInSpec = tween(240), fadeOutSpec = tween(240))
                        .padding(horizontal = 20.dp)
                        .padding(top = 6.dp)
                )
            }
        }
        val tracks = state.visible
        if (state.isIncomplete) {
            item(key = "incomplete") {
                Text(
                    strings.search.incomplete,
                    style = LamplightType.bodyS(),
                    color = LamplightTheme.colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
        when {
            state.error != null && state.tracks.isEmpty() -> item(key = "error") { Message(state.error.resolve(strings), l.retry, actions.onRetry) }
            !state.isLoading && tracks.isEmpty() ->
                item(key = "empty") { Message(if (state.view == ListenView.DOWNLOADED) l.emptyDownloads else l.emptyAll, null, {}) }
        }
        items(tracks, key = { it.track.id }) { item ->
            val track = item.track
            val active = audio.trackOrNull?.id == track.id
            val disc = if (active) audio.disc() else DiscState.Idle
            val position = (audio as? AudioState.Playing)?.positionMs ?: (audio as? AudioState.Paused)?.positionMs ?: 0L
            val duration = knownDurations[track.id]
            val unavailable = !online && item.download != DownloadState.Downloaded && !active
            val label = listOfNotNull(item.entry.typeLabel(strings), item.areaKey.areaLabel(strings).takeIf { it.isNotBlank() })
                .joinToString(" · ").uppercaseFor(lang)
            val time = when {
                active -> "${position.toClockDuration() ?: "0:00"} / ${duration?.toClockDuration() ?: "–:––"}"
                else -> duration?.toClockDuration()
            }
            val availability = when (val d = item.download) {
                is DownloadState.Downloading -> l.downloading.replace("{n}", ((d.progress ?: 0f) * 100).toInt().toString())
                DownloadState.Downloaded -> l.downloaded
                DownloadState.NotDownloaded -> if (online) l.streamsOnline else l.needsInternet
            }
            val meta = listOfNotNull(time, availability, if (track.isEnglishFallback) "EN · ${l.englishAudio}" else null).joinToString(" · ")
            ListenTrackRow(
                label = label,
                title = track.title,
                meta = meta,
                photoUrl = item.entry.photo?.let(::buildPhotoUrl),
                disc = disc,
                active = active,
                unavailable = unavailable,
                playDescription = playDescription(disc, track.title, position, duration, l),
                download = item.download,
                downloadDescription = when (val d = item.download) {
                    DownloadState.NotDownloaded -> l.downloadFor.replace("{title}", track.title)
                    is DownloadState.Downloading -> l.downloadingTapToCancel.replace("{n}", ((d.progress ?: 0f) * 100).toInt().toString())
                    DownloadState.Downloaded -> l.downloadedTapToRemove
                },
                reduceMotion = reduceMotion,
                onPlay = { actions.onPlay(item) },
                onOpen = { actions.onOpen(item) },
                onDownload = { actions.onDownload(item) },
                modifier = Modifier.animateItem().padding(horizontal = 20.dp)
            )
        }
    }

    removing?.let { item ->
        val colors = LamplightTheme.colors
        AlertDialog(
            onDismissRequest = actions.onDismissRemove,
            containerColor = colors.surfaceRaised,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = { Text(l.removeTitle, style = LamplightType.cardTitle()) },
            text = {
                Text(
                    if (item.hasOtherLanguages) l.removeBodyLanguage.replace("{lang}", item.track.lang.uppercase()) else l.removeBody,
                    style = LamplightType.bodyS()
                )
            },
            confirmButton = {
                TextButton(onClick = { actions.onConfirmRemove(item) }) {
                    Text(l.remove, color = colors.lamp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissRemove) { Text(l.cancel, color = colors.textPrimary) }
            }
        )
    }
}

@Composable
private fun TopBar(view: ListenView, l: ListenStrings, backLabel: String, actions: ListenActions) {
    val colors = LamplightTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.lamp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.night)
                    .clickable(onClick = actions.onBack)
                    .semantics { role = Role.Button; contentDescription = backLabel },
                contentAlignment = Alignment.Center
            ) { ChevronLeftIcon(color = colors.lamp, size = 18.dp) }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeadphonesIcon(colors.night, 14.dp)
                Text(l.freeAudio, style = LamplightType.label(), color = colors.night)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                l.title,
                style = LamplightType.areaTitle().copy(fontSize = 44.sp, lineHeight = 44.sp),
                color = colors.night,
                modifier = Modifier.semantics { heading() }
            )
            Text(l.subtitle, style = LamplightType.body().copy(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = colors.night)
        }
        LamplightSegmentedControl(
            options = listOf(SegmentOption(l.allAudio), SegmentOption(l.downloaded)),
            selectedIndex = if (view == ListenView.ALL) 0 else 1,
            onSelect = { actions.onView(if (it == 0) ListenView.ALL else ListenView.DOWNLOADED) }
        )
    }
}

@Composable
private fun Message(text: String, action: String?, onAction: () -> Unit) {
    val colors = LamplightTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text, style = LamplightType.body().copy(fontSize = 15.sp, lineHeight = 23.sp), color = HomePalette.onGradientSecondary, textAlign = TextAlign.Center)
        if (action != null) {
            Text(
                action,
                style = LamplightType.body().copy(fontWeight = FontWeight.SemiBold),
                color = colors.lamp,
                modifier = Modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onAction).padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

private fun AudioState.disc(): DiscState = when (this) {
    is AudioState.Loading -> DiscState.Loading
    is AudioState.Playing -> DiscState.Playing
    is AudioState.Paused -> DiscState.Paused
    is AudioState.Failed -> DiscState.Failed
    AudioState.Idle -> DiscState.Idle
}

private fun playDescription(disc: DiscState, title: String, positionMs: Long, durationMs: Long?, l: ListenStrings): String = when (disc) {
    DiscState.Playing -> l.pause.replace("{title}", title)
    DiscState.Paused -> l.resume.replace("{title}", title).replace("{elapsed}", positionMs.toClockDuration() ?: "0:00")
    DiscState.Loading -> l.loadingTapToCancel.replace("{title}", title)
    DiscState.Idle, DiscState.Failed -> {
        val duration = durationMs?.toClockDuration()
        if (duration != null) l.play.replace("{title}", title).replace("{duration}", duration) else l.play.replace("{title}", title).replace(", {duration}", "")
    }
}

/** "Cathedral" for a place with a curated subtype, else "Place" / "Activity" / "Story". */
private fun AudioCatalogEntry.typeLabel(strings: AppStrings): String = subtype?.toTitleCaseLabel() ?: when (source) {
    AudioSource.PLACE -> strings.search.place
    AudioSource.ACTIVITY -> strings.search.activity
    AudioSource.STORY -> strings.search.story
}
