package llc.bokadev.kompass.presentation.placedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_audio_not_downloaded
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.retry
import llc.bokadev.kompass.core.audio.AudioController
import llc.bokadev.kompass.core.audio.NavAudioContext
import llc.bokadev.kompass.core.audio.NavAudioHost
import llc.bokadev.kompass.core.audio.onNavAudioTap
import llc.bokadev.kompass.core.audio.resolveNavAudioMode
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.excursions.related_activities.RelatedActivities
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.detail.DetailLoadState
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import llc.bokadev.kompass.presentation.shared.rememberLiveCurrentLocation
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.random.Random

/**
 * Orchestrator: ViewModel, state, live location, hand-offs, and screen-lifetime audio wiring.
 * [autoplay] (Home "Listen") plays the place once its track is known.
 */
@Composable
fun PlaceDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: PlaceDetailViewModel,
    autoplay: Boolean = false,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.placeDetailState.collectAsStateWithLifecycle()
    val navAudio = koinInject<NavAudioHost>()
    val audio = koinInject<AudioController>()
    val snackbarBus = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    // perform() still takes the AppStrings tables until it is migrated.
    val strings = appStringsFor(LocalAppLocale.current.substringBefore('-'))
    val currentLocation = rememberLiveCurrentLocation(koinInject<UserLocationProvider>(), 5_000L, state.place?.id)

    // The page is `night` from top to bottom, so the status bar icons stay light throughout.
    StatusBarIconsEffect(lightIcons = true)

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is PlaceDetailEvent.OpenExternal) {
            launcher.perform(event.launch, snackbarBus, strings)
        }
    }

    // The Listen control plays this place (this screen owns playback); leaving stops it.
    val ownerKey = remember(viewModel.id) { "place:${viewModel.id}:${Random.nextLong()}" }
    // Home "Listen": play once when the track is known; not again after rotation or returning.
    var autoplayPending by rememberSaveable(viewModel.id) { mutableStateOf(autoplay) }
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

    val audioState by audio.state.collectAsStateWithLifecycle()
    val online by koinInject<ConnectivityMonitor>().isOnline.collectAsStateWithLifecycle()
    val audioMode = state.audioTrack?.let { resolveNavAudioMode(NavAudioContext(ownerKey, it), audioState, offline = !online) }
    val audioNotDownloaded = stringResource(Res.string.detail_audio_not_downloaded)

    val retryLabel = stringResource(Res.string.retry)
    val backLabel = stringResource(Res.string.detail_back)
    val onRetryClick = { viewModel.onEvent(PlaceDetailEvent.OnRetryClick) }
    val onBackClick = { viewModel.onEvent(PlaceDetailEvent.NavigateBack) }

    BaseContentView(
        modifier = modifier.pageGradientBackground(),
        viewState = state,
        loading = { DetailLoadState(null, retryLabel, backLabel, onRetryClick, onBackClick) },
        error = { message -> DetailLoadState(message, retryLabel, backLabel, onRetryClick, onBackClick) }
    ) {
        PlaceDetailScreenContent(
            state = state,
            currentLocation = currentLocation,
            audioMode = audioMode,
            onBackClick = onBackClick,
            onSavedChange = { viewModel.onEvent(PlaceDetailEvent.OnSavedChange(it)) },
            onMapsClick = { viewModel.onEvent(PlaceDetailEvent.OnMapsClick) },
            onMenuClick = { viewModel.onEvent(PlaceDetailEvent.OnMenuClick(it)) },
            onCallClick = { viewModel.onEvent(PlaceDetailEvent.OnCallClick(it)) },
            onUserScrolled = { viewModel.onEvent(PlaceDetailEvent.OnUserScrolled) },
            onListenClick = {
                audioMode?.let { audio.onNavAudioTap(it, ownerKey) { showSnackBar(audioNotDownloaded) } }
            },
            relatedActivities = { RelatedActivities(viewModel.id) }
        )
    }
}
