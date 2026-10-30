package llc.bokadev.kompass.presentation.screens.experiencedetail

import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.audio.resolveNavAudioMode
import llc.bokadev.kompass.core.audio.onNavAudioTap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import llc.bokadev.kompass.core.audio.AudioController
import llc.bokadev.kompass.core.audio.NavAudioContext
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import llc.bokadev.kompass.presentation.shared.rememberLiveCurrentLocation
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.random.Random

/** Orchestrator: ViewModel, state, live location, hand-offs, and screen-lifetime audio wiring. */
@Composable
fun ExperienceDetailScreen(id: String) {
    val vm: ExperienceDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()
    val navAudio = koinInject<NavAudioHost>()
    val audio = koinInject<AudioController>()
    val snackbar = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    val strings = rememberAppStrings()
    val currentLocation = rememberLiveCurrentLocation(koinInject<UserLocationProvider>(), 5_000L, state.activity?.id)

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is ExperienceDetailEvent.Launch -> launcher.perform(event.launch, snackbar, strings)
            }
        }
    }

    // The Listen control plays this activity (this screen owns playback); leaving stops it.
    val ownerKey = remember(id) { "activity:$id:${Random.nextLong()}" }
    DisposableEffect(ownerKey, state.audioTrack) {
        navAudio.push(NavAudioContext(ownerKey, state.audioTrack))
        onDispose {
            navAudio.pop(ownerKey)
            audio.stopIfOwner(ownerKey)
        }
    }

    val audioState by audio.state.collectAsState()
    val online by koinInject<ConnectivityMonitor>().isOnline.collectAsState()
    val audioMode = state.audioTrack?.let { resolveNavAudioMode(NavAudioContext(ownerKey, it), audioState, offline = !online) }

    ExperienceDetailScreenContent(
        state = state,
        currentLocation = currentLocation,
        onAction = { action ->
            if (action == ExperienceDetailAction.Listen) {
                audioMode?.let { audio.onNavAudioTap(it, ownerKey) { snackbar.show(strings.detail.audioNotDownloaded) } }
            } else {
                vm.onAction(action)
            }
        },
        audioMode = audioMode
    )
}
