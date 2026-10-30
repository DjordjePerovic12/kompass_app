package llc.bokadev.kompass.presentation.screens.listen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import llc.bokadev.kompass.core.audio.AudioController
import llc.bokadev.kompass.core.audio.AudioState
import llc.bokadev.kompass.core.audio.NavAudioContext
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.util.ConnectivityMonitor
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random

/**
 * Listen orchestrator. The ViewModel owns the list, downloads and navigation; this composable owns
 * what is tied to the screen's lifetime: the library's playback key (leaving stops playback,
 * backgrounding doesn't), the Now playing card, and the remove confirmation.
 */
@Composable
fun ListenScreen() {
    val vm: ListenViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val audio = koinInject<AudioController>()
    val audioState by audio.state.collectAsState()
    val navAudio = koinInject<NavAudioHost>()
    val snackbar = koinInject<SnackbarBus>()
    val online by koinInject<ConnectivityMonitor>().isOnline.collectAsState()
    val strings = rememberAppStrings()
    val l = strings.listen
    val ownerKey = rememberSaveable { "library:${Random.nextLong()}" }
    val knownDurations = remember { mutableStateMapOf<String, Long>() }
    var shown by remember { mutableStateOf<AudioState?>(null) }
    var removingId by remember { mutableStateOf<String?>(null) }

    StatusBarIconsEffect(lightIcons = false)
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
                if (s is AudioState.Failed) snackbar.show(l.couldntPlaySnack)
            }
            shown != null -> {
                // Track ended (or stopped elsewhere): the card lingers for 1s, then hides. No auto-play.
                delay(1_000)
                shown = null
            }
        }
    }
    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                ListenEvent.DownloadFailed -> snackbar.show(l.downloadFailed)
                is ListenEvent.ConfirmRemoval -> removingId = event.trackId
            }
        }
    }

    ListenScreenContent(
        state = state,
        nowPlaying = shown,
        audio = audioState,
        online = online,
        knownDurations = knownDurations,
        removing = removingId?.let { id -> state.tracks.firstOrNull { it.track.id == id } },
        reduceMotion = rememberReduceMotion(),
        strings = strings,
        lang = currentAppLanguage(),
        onAction = { action ->
            when (action) {
                is ListenAction.Play -> {
                    val current = audioState.trackOrNull?.id == action.item.track.id
                    if (!online && !action.item.track.isDownloaded && !current) snackbar.show(l.connectToPlay)
                    else audio.toggle(action.item.track, ownerKey)
                }
                ListenAction.ToggleCurrent -> audioState.trackOrNull?.let { audio.toggle(it, ownerKey) }
                ListenAction.Stop -> {
                    shown = null
                    audio.stopIfOwner(ownerKey)
                }
                is ListenAction.Seek -> audio.seekTo(action.positionMs)
                is ListenAction.Skip -> audio.skipBy(action.deltaMs)
                ListenAction.OpenCurrent ->
                    (shown?.trackOrNull ?: audioState.trackOrNull)?.let { vm.onAction(ListenAction.Open(it.source, it.sourceId)) }
                ListenAction.DismissRemove -> removingId = null
                is ListenAction.ConfirmRemove -> {
                    removingId = null
                    vm.onAction(action)
                }
                else -> vm.onAction(action)
            }
        }
    )
}
