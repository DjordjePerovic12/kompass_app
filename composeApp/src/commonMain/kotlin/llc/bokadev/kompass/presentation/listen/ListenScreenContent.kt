package llc.bokadev.kompass.presentation.listen

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.listen_all_audio
import kompass.composeapp.generated.resources.listen_back15
import kompass.composeapp.generated.resources.listen_cancel
import kompass.composeapp.generated.resources.listen_couldnt_play
import kompass.composeapp.generated.resources.listen_download_for
import kompass.composeapp.generated.resources.listen_downloaded
import kompass.composeapp.generated.resources.listen_downloaded_tap_to_remove
import kompass.composeapp.generated.resources.listen_downloading
import kompass.composeapp.generated.resources.listen_downloading_tap_to_cancel
import kompass.composeapp.generated.resources.listen_empty_all
import kompass.composeapp.generated.resources.listen_empty_downloads
import kompass.composeapp.generated.resources.listen_english_audio
import kompass.composeapp.generated.resources.listen_forward15
import kompass.composeapp.generated.resources.listen_free_audio
import kompass.composeapp.generated.resources.listen_loading
import kompass.composeapp.generated.resources.listen_loading_tap_to_cancel
import kompass.composeapp.generated.resources.listen_needs_internet
import kompass.composeapp.generated.resources.listen_now_playing
import kompass.composeapp.generated.resources.listen_now_playing_region
import kompass.composeapp.generated.resources.listen_pause
import kompass.composeapp.generated.resources.listen_paused
import kompass.composeapp.generated.resources.listen_play
import kompass.composeapp.generated.resources.listen_progress_value
import kompass.composeapp.generated.resources.listen_remove
import kompass.composeapp.generated.resources.listen_remove_body
import kompass.composeapp.generated.resources.listen_remove_body_language
import kompass.composeapp.generated.resources.listen_remove_title
import kompass.composeapp.generated.resources.listen_resume
import kompass.composeapp.generated.resources.listen_retry
import kompass.composeapp.generated.resources.listen_stop
import kompass.composeapp.generated.resources.listen_streams_online
import kompass.composeapp.generated.resources.listen_subtitle
import kompass.composeapp.generated.resources.listen_title
import kompass.composeapp.generated.resources.search_activity
import kompass.composeapp.generated.resources.search_incomplete
import kompass.composeapp.generated.resources.search_place
import kompass.composeapp.generated.resources.search_story
import llc.bokadev.kompass.core.audio.AudioState
import llc.bokadev.kompass.core.audio.AudioTrack
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.toClockDuration
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.AudioCatalogEntry
import llc.bokadev.kompass.domain.model.AudioSource
import llc.bokadev.kompass.presentation.listen.components.DiscState
import llc.bokadev.kompass.presentation.listen.components.ListenTrackRow
import llc.bokadev.kompass.presentation.listen.components.NowPlayingCard
import llc.bokadev.kompass.presentation.listen.components.NowPlayingLabels
import llc.bokadev.kompass.presentation.shared.icons.ChevronLeftIcon
import llc.bokadev.kompass.presentation.shared.icons.HeadphonesIcon
import llc.bokadev.kompass.presentation.shared.LamplightSegmentedControl
import llc.bokadev.kompass.presentation.shared.SegmentOption
import llc.bokadev.kompass.presentation.shared.areaLabel
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource
import llc.bokadev.kompass.presentation.shared.StatusBarScrim
import androidx.compose.foundation.lazy.rememberLazyListState

/**
 * Lamplight audio library ("Listen"): play in place, one active track, a Now playing card while
 * something is loaded, and honest Downloaded / Streams online / Needs internet labels.
 */
@Composable
fun ListenScreenContent(
    modifier: Modifier = Modifier,
    state: ListenState,
    /** What the Now playing card shows: the live state, or the last one for 1s after a track ends. */
    nowPlaying: AudioState?,
    audio: AudioState,
    online: Boolean,
    /** Durations learned from the player this session, by track id (the CMS has none). */
    knownDurations: Map<String, Long>,
    removing: ListenTrack?,
    reduceMotion: Boolean,
    onBackClick: () -> Unit,
    onViewSelect: (ListenView) -> Unit,
    onPlayClick: (ListenTrack) -> Unit,
    onTrackClick: (ListenTrack) -> Unit,
    onDownloadClick: (ListenTrack) -> Unit,
    onRemoveConfirm: (ListenTrack) -> Unit,
    onRemoveDismiss: () -> Unit,
    onToggleCurrentClick: () -> Unit,
    onStopClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onNowPlayingClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val l = listenLabels()
    val listState = rememberLazyListState()
    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().pageGradientBackground(),
            state = listState,
            contentPadding = PaddingValues(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "top") { TopBar(state.view, l, onBackClick, onViewSelect) }
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
                        onToggle = onToggleCurrentClick,
                        onStop = onStopClick,
                        onSeek = onSeek,
                        onSkip = onSkip,
                        onOpen = onNowPlayingClick,
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
                        l.incomplete,
                        style = KompassTheme.typography.geologicaRegular14,
                        color = KompassTheme.colors.stoneGray,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
            val error = state.error
            when {
                error != null && state.tracks.isEmpty() -> item(key = "error") { Message(stringResource(error.messageRes), l.retry, onRetryClick) }
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
                val label = listOfNotNull(item.entry.typeLabel(l), item.areaKey.areaLabel().takeIf { it.isNotBlank() })
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
                    onPlay = { onPlayClick(item) },
                    onOpen = { onTrackClick(item) },
                    onDownload = { onDownloadClick(item) },
                    modifier = Modifier.animateItem().padding(horizontal = 20.dp)
                )
            }
        }
        StatusBarScrim(visible = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0)
    }

    removing?.let { item ->
        val colors = KompassTheme.colors
        AlertDialog(
            onDismissRequest = onRemoveDismiss,
            containerColor = colors.charcoal,
            titleContentColor = colors.boneWhite,
            textContentColor = colors.stoneGray,
            title = { Text(l.removeTitle, style = KompassTheme.typography.gloockRegular23) },
            text = {
                Text(
                    if (item.hasOtherLanguages) l.removeBodyLanguage.replace("{lang}", item.track.lang.uppercase()) else l.removeBody,
                    style = KompassTheme.typography.geologicaRegular14
                )
            },
            confirmButton = {
                TextButton(onClick = { onRemoveConfirm(item) }) {
                    Text(l.remove, color = colors.lampYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onRemoveDismiss) { Text(l.cancel, color = colors.boneWhite) }
            }
        )
    }
}

/**
 * The library's copy. Templated strings are resolved with their "{title}" / "{duration}" / "{elapsed}" /
 * "{n}" / "{lang}" placeholders kept, and filled per row (the play label drops ", {duration}" when unknown).
 */
private data class ListenLabels(
    val freeAudio: String,
    val title: String,
    val subtitle: String,
    val allAudio: String,
    val downloaded: String,
    val streamsOnline: String,
    val needsInternet: String,
    val downloading: String,
    val englishAudio: String,
    val loading: String,
    val nowPlaying: String,
    val paused: String,
    val couldntPlay: String,
    val retry: String,
    val stop: String,
    val back15: String,
    val forward15: String,
    val nowPlayingRegion: String,
    val play: String,
    val pause: String,
    val resume: String,
    val loadingTapToCancel: String,
    val downloadFor: String,
    val downloadingTapToCancel: String,
    val downloadedTapToRemove: String,
    val removeTitle: String,
    val removeBody: String,
    val removeBodyLanguage: String,
    val remove: String,
    val cancel: String,
    val emptyDownloads: String,
    val emptyAll: String,
    val progressValue: String,
    val back: String,
    val incomplete: String,
    val place: String,
    val activity: String,
    val story: String
)

@Composable
private fun listenLabels() = ListenLabels(
    freeAudio = stringResource(Res.string.listen_free_audio),
    title = stringResource(Res.string.listen_title),
    subtitle = stringResource(Res.string.listen_subtitle),
    allAudio = stringResource(Res.string.listen_all_audio),
    downloaded = stringResource(Res.string.listen_downloaded),
    streamsOnline = stringResource(Res.string.listen_streams_online),
    needsInternet = stringResource(Res.string.listen_needs_internet),
    downloading = stringResource(Res.string.listen_downloading, "{n}"),
    englishAudio = stringResource(Res.string.listen_english_audio),
    loading = stringResource(Res.string.listen_loading),
    nowPlaying = stringResource(Res.string.listen_now_playing),
    paused = stringResource(Res.string.listen_paused),
    couldntPlay = stringResource(Res.string.listen_couldnt_play),
    retry = stringResource(Res.string.listen_retry),
    stop = stringResource(Res.string.listen_stop),
    back15 = stringResource(Res.string.listen_back15),
    forward15 = stringResource(Res.string.listen_forward15),
    nowPlayingRegion = stringResource(Res.string.listen_now_playing_region),
    play = stringResource(Res.string.listen_play, "{title}", "{duration}"),
    pause = stringResource(Res.string.listen_pause, "{title}"),
    resume = stringResource(Res.string.listen_resume, "{title}", "{elapsed}"),
    loadingTapToCancel = stringResource(Res.string.listen_loading_tap_to_cancel, "{title}"),
    downloadFor = stringResource(Res.string.listen_download_for, "{title}"),
    downloadingTapToCancel = stringResource(Res.string.listen_downloading_tap_to_cancel, "{n}"),
    downloadedTapToRemove = stringResource(Res.string.listen_downloaded_tap_to_remove),
    removeTitle = stringResource(Res.string.listen_remove_title),
    removeBody = stringResource(Res.string.listen_remove_body),
    removeBodyLanguage = stringResource(Res.string.listen_remove_body_language, "{lang}"),
    remove = stringResource(Res.string.listen_remove),
    cancel = stringResource(Res.string.listen_cancel),
    emptyDownloads = stringResource(Res.string.listen_empty_downloads),
    emptyAll = stringResource(Res.string.listen_empty_all),
    progressValue = stringResource(Res.string.listen_progress_value, "{elapsed}", "{duration}"),
    back = stringResource(Res.string.detail_back),
    incomplete = stringResource(Res.string.search_incomplete),
    place = stringResource(Res.string.search_place),
    activity = stringResource(Res.string.search_activity),
    story = stringResource(Res.string.search_story)
)

@Composable
private fun TopBar(view: ListenView, l: ListenLabels, onBackClick: () -> Unit, onViewSelect: (ListenView) -> Unit) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.lampYellow)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.nightBlack)
                    .clickable(onClick = onBackClick)
                    .semantics { role = Role.Button; contentDescription = l.back },
                contentAlignment = Alignment.Center
            ) { ChevronLeftIcon(color = colors.lampYellow, size = 18.dp) }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeadphonesIcon(colors.nightBlack, 14.dp)
                Text(l.freeAudio, style = KompassTheme.typography.geologicaSemiBold11, color = colors.nightBlack)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                l.title,
                style = KompassTheme.typography.gloockRegular40.copy(fontSize = 44.sp, lineHeight = 44.sp),
                color = colors.nightBlack,
                modifier = Modifier.semantics { heading() }
            )
            Text(l.subtitle, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = colors.nightBlack)
        }
        LamplightSegmentedControl(
            options = listOf(SegmentOption(l.allAudio), SegmentOption(l.downloaded)),
            selectedIndex = if (view == ListenView.ALL) 0 else 1,
            onSelect = { onViewSelect(if (it == 0) ListenView.ALL else ListenView.DOWNLOADED) }
        )
    }
}

@Composable
private fun Message(text: String, action: String?, onAction: () -> Unit) {
    val colors = KompassTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 23.sp), color = KompassTheme.colors.white85, textAlign = TextAlign.Center)
        if (action != null) {
            Text(
                action,
                style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.SemiBold),
                color = colors.lampYellow,
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

private fun playDescription(disc: DiscState, title: String, positionMs: Long, durationMs: Long?, l: ListenLabels): String = when (disc) {
    DiscState.Playing -> l.pause.replace("{title}", title)
    DiscState.Paused -> l.resume.replace("{title}", title).replace("{elapsed}", positionMs.toClockDuration() ?: "0:00")
    DiscState.Loading -> l.loadingTapToCancel.replace("{title}", title)
    DiscState.Idle, DiscState.Failed -> {
        val duration = durationMs?.toClockDuration()
        if (duration != null) l.play.replace("{title}", title).replace("{duration}", duration) else l.play.replace("{title}", title).replace(", {duration}", "")
    }
}

/** "Cathedral" for a place with a curated subtype, else "Place" / "Activity" / "Story". */
private fun AudioCatalogEntry.typeLabel(l: ListenLabels): String = subtype?.toTitleCaseLabel() ?: when (source) {
    AudioSource.PLACE -> l.place
    AudioSource.ACTIVITY -> l.activity
    AudioSource.STORY -> l.story
}

@Preview
@Composable
private fun ListenScreenContentPreview() {
    val entry = AudioCatalogEntry(
        source = AudioSource.PLACE, id = "cathedral", title = mapOf("en" to "Cathedral of Saint Tryphon"),
        area = null, zone = "old_town", latitude = 42.4247, longitude = 18.7712,
        files = mapOf("en" to "cathedral-en.mp3"), photo = null, subtype = "cathedral", sortOrder = 0
    )
    val track = AudioTrack(
        source = AudioSource.PLACE, sourceId = "cathedral", title = "Cathedral of Saint Tryphon", subtitle = "old_town",
        audioPath = "cathedral-en.mp3", lang = "en", isEnglishFallback = false, isDownloaded = true
    )
    KompassTheme {
        ListenScreenContent(
            state = ListenState(
                isLoading = false,
                tracks = listOf(ListenTrack(track, entry, "old_town", hasOtherLanguages = false, download = DownloadState.Downloaded))
            ),
            nowPlaying = AudioState.Playing(track, positionMs = 65_000, durationMs = 252_000),
            audio = AudioState.Playing(track, positionMs = 65_000, durationMs = 252_000),
            online = true,
            knownDurations = mapOf(track.id to 252_000L),
            removing = null,
            reduceMotion = true,
            onBackClick = {},
            onViewSelect = {},
            onPlayClick = {},
            onTrackClick = {},
            onDownloadClick = {},
            onRemoveConfirm = {},
            onRemoveDismiss = {},
            onToggleCurrentClick = {},
            onStopClick = {},
            onSeek = {},
            onSkip = {},
            onNowPlayingClick = {},
            onRetryClick = {}
        )
    }
}
