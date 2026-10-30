package llc.bokadev.kompass.presentation.screens.placedetail

import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.audio.resolveNavAudioMode
import llc.bokadev.kompass.core.audio.onNavAudioTap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import llc.bokadev.kompass.core.audio.AudioController
import llc.bokadev.kompass.core.audio.NavAudioContext
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.presentation.excursions.related_activities.RelatedActivities
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import llc.bokadev.kompass.presentation.shared.rememberLiveCurrentLocation
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.random.Random

/**
 * Orchestrator: ViewModel, state, live location, external hand-offs, and the screen-lifetime
 * audio wiring: the Listen control plays this place (handled here, the screen owns playback);
 * leaving the screen stops it.
 */
@Composable
fun PlaceDetailScreen(id: String, autoplay: Boolean = false) {
    val vm: PlaceDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()
    val navAudio = koinInject<NavAudioHost>()
    val audio = koinInject<AudioController>()
    val snackbar = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    val strings = rememberAppStrings()
    val currentLocation = rememberLiveCurrentLocation(koinInject<UserLocationProvider>(), 5_000L, state.place?.id)

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is PlaceDetailEvent.Launch -> launcher.perform(event.launch, snackbar, strings)
            }
        }
    }

    val ownerKey = remember(id) { "place:$id:${Random.nextLong()}" }
    // Home "Listen": play once when the track is known; not again after rotation or returning.
    var autoplayPending by rememberSaveable(id) { mutableStateOf(autoplay) }
    LaunchedEffect(state.audioTrack) {
        val track = state.audioTrack ?: return@LaunchedEffect
        if (autoplayPending) {
            autoplayPending = false
            audio.play(track, ownerKey)
        }
    }
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

    PlaceDetailScreenContent(
        state = state,
        currentLocation = currentLocation,
        onAction = { action ->
            if (action == PlaceDetailAction.Listen) {
                audioMode?.let { audio.onNavAudioTap(it, ownerKey) { snackbar.show(strings.detail.audioNotDownloaded) } }
            } else {
                vm.onAction(action)
            }
        },
        relatedActivities = { RelatedActivities(id) },
        audioMode = audioMode
    )
}
