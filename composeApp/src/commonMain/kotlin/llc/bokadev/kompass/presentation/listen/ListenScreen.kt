package llc.bokadev.kompass.presentation.listen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.listen_connect_to_play
import kompass.composeapp.generated.resources.listen_couldnt_play_snack
import kompass.composeapp.generated.resources.listen_download_failed
import kotlinx.coroutines.delay
import llc.bokadev.kompass.core.audio.AudioController
import llc.bokadev.kompass.core.audio.AudioState
import llc.bokadev.kompass.core.audio.NavAudioContext
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.random.Random

/**
 * Orchestrator. The ViewModel owns the list, downloads and navigation; this composable owns what is
 * tied to the screen's lifetime: the library's playback key (leaving stops playback, backgrounding
 * doesn't), the Now playing card, and the remove confirmation.
 */
@Composable
fun ListenScreen(
    modifier: Modifier = Modifier,
    viewModel: ListenViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.listenState.collectAsStateWithLifecycle()
    val audio = koinInject<AudioController>()
    val audioState by audio.state.collectAsStateWithLifecycle()
    val navAudio = koinInject<NavAudioHost>()
    val online by koinInject<ConnectivityMonitor>().isOnline.collectAsStateWithLifecycle()
    val ownerKey = rememberSaveable { "library:${Random.nextLong()}" }
    val knownDurations = remember { mutableStateMapOf<String, Long>() }
    var shown by remember { mutableStateOf<AudioState?>(null) }
    var removingId by remember { mutableStateOf<String?>(null) }
    val couldntPlay = stringResource(Res.string.listen_couldnt_play_snack)
    val connectToPlay = stringResource(Res.string.listen_connect_to_play)

    StatusBarIconsEffect(lightIcons = false)

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        when (event) {
            ListenEvent.ShowDownloadFailed -> showSnackBar(getString(Res.string.listen_download_failed))
            is ListenEvent.ConfirmRemoval -> removingId = event.trackId
            else -> Unit
        }
    }

    DisposableEffect(ownerKey) {
        navAudio.push(NavAudioContext(ownerKey, track = null, isLibrary = true))
        onDispose {
            navAudio.pop(ownerKey)
            audio.stopIfOwner(ownerKey)
        }
    }
    LaunchedEffect(audioState) {
        val s = audioState
        when {
            s.trackOrNull != null -> {
                shown = s
                ((s as? AudioState.Playing)?.durationMs ?: (s as? AudioState.Paused)?.durationMs)
                    ?.takeIf { it > 0 }?.let { knownDurations[s.trackOrNull!!.id] = it }
                if (s is AudioState.Failed) showSnackBar(couldntPlay)
            }
            shown != null -> {
                // Track ended (or stopped elsewhere): the card lingers for 1s, then hides. No auto-play.
                delay(1_000)
                shown = null
            }
        }
    }

    // The list keeps its top bar while loading or failing (the error row is part of it),
    // so every BaseContentView slot shows the same content.
    val content: @Composable () -> Unit = {
        ListenScreenContent(
            state = state,
            nowPlaying = shown,
            audio = audioState,
            online = online,
            knownDurations = knownDurations,
            removing = removingId?.let { id -> state.tracks.firstOrNull { it.track.id == id } },
            reduceMotion = rememberReduceMotion(),
            onBackClick = { viewModel.onEvent(ListenEvent.NavigateBack) },
            onViewSelect = { viewModel.onEvent(ListenEvent.OnViewSelect(it)) },
            onPlayClick = { item ->
                val current = audioState.trackOrNull?.id == item.track.id
                if (!online && !item.track.isDownloaded && !current) showSnackBar(connectToPlay)
                else audio.toggle(item.track, ownerKey)
            },
            onTrackClick = { viewModel.onEvent(ListenEvent.OnTrackClick(it.entry.source, it.entry.id)) },
            onDownloadClick = { viewModel.onEvent(ListenEvent.OnDownloadClick(it)) },
            onRemoveConfirm = {
                removingId = null
                viewModel.onEvent(ListenEvent.OnRemoveConfirm(it))
            },
            onRemoveDismiss = { removingId = null },
            onToggleCurrentClick = { audioState.trackOrNull?.let { audio.toggle(it, ownerKey) } },
            onStopClick = {
                shown = null
                audio.stopIfOwner(ownerKey)
            },
            onSeek = { audio.seekTo(it) },
            onSkip = { audio.skipBy(it) },
            onNowPlayingClick = {
                (shown?.trackOrNull ?: audioState.trackOrNull)?.let { viewModel.onEvent(ListenEvent.OnTrackClick(it.source, it.sourceId)) }
            },
            onRetryClick = { viewModel.onEvent(ListenEvent.OnRetryClick) }
        )
    }

    BaseContentView(
        modifier = modifier,
        viewState = state,
        loading = content,
        error = { content() }
    ) {
        content()
    }
}
