package llc.bokadev.kompass.presentation.screens.localfinds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.random.Random
import llc.bokadev.kompass.core.audio.AudioController
import llc.bokadev.kompass.core.audio.NavAudioContext
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.audio.onNavAudioTap
import llc.bokadev.kompass.core.audio.resolveNavAudioMode
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.util.ConnectivityMonitor
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Orchestrator: ViewModel, state, hand-offs, and the story's audio wiring. Same rules as place
 * detail: the Listen pill plays this story, and leaving stops our playback. Listen is handled
 * here because playback is owned by this screen.
 */
@Composable
fun LocalFindDetailScreen(id: String, filter: String? = null) {
    val vm: LocalFindDetailViewModel = koinViewModel(key = "story-$id", parameters = { parametersOf(id, filter) })
    val state by vm.state.collectAsState()
    val navAudio = koinInject<NavAudioHost>()
    val audio = koinInject<AudioController>()
    val audioState by audio.state.collectAsState()
    val online by koinInject<ConnectivityMonitor>().isOnline.collectAsState()
    val snackbar = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    val strings = rememberAppStrings()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is LocalFindDetailEvent.Launch -> launcher.perform(event.launch, snackbar, strings)
            }
        }
    }

    val ownerKey = remember(id) { "story:$id:${Random.nextLong()}" }
    DisposableEffect(ownerKey, state.audioTrack) {
        navAudio.push(NavAudioContext(ownerKey, state.audioTrack))
        onDispose {
            navAudio.pop(ownerKey)
            audio.stopIfOwner(ownerKey)
        }
    }
    val audioMode = state.audioTrack?.let { resolveNavAudioMode(NavAudioContext(ownerKey, it), audioState, offline = !online) }

    LocalFindDetailScreenContent(
        state = state,
        audioMode = audioMode,
        strings = strings,
        lang = currentAppLanguage(),
        onAction = { action ->
            if (action == LocalFindDetailAction.Listen) {
                audioMode?.let { audio.onNavAudioTap(it, ownerKey) { snackbar.show(strings.detail.audioNotDownloaded) } }
            } else {
                vm.onAction(action)
            }
        }
    )
}
