package llc.bokadev.kompass.presentation.localfinds.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.detail.DetailLoadState
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.random.Random

/**
 * Orchestrator: ViewModel, state, hand-offs, and the story's audio wiring. Same rules as place
 * detail: the Listen pill plays this story, and leaving stops our playback. Listen is handled
 * here because playback is owned by this screen.
 */
@Composable
fun LocalFindDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: LocalFindDetailViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.localFindDetailState.collectAsStateWithLifecycle()
    val navAudio = koinInject<NavAudioHost>()
    val audio = koinInject<AudioController>()
    val snackbarBus = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is LocalFindDetailEvent.OpenExternal) {
            launcher.perform(event.launch, snackbarBus)
        }
    }

    // The Listen pill plays this story (this screen owns playback); leaving stops it.
    val ownerKey = remember(viewModel.id) { "story:${viewModel.id}:${Random.nextLong()}" }
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
    val onRetryClick = { viewModel.onEvent(LocalFindDetailEvent.OnRetryClick) }
    val onBackClick = { viewModel.onEvent(LocalFindDetailEvent.NavigateBack) }

    BaseContentView(
        modifier = modifier.pageGradientBackground(),
        viewState = state,
        loading = {
            StatusBarIconsEffect(lightIcons = true)
            DetailLoadState(null, retryLabel, backLabel, onRetryClick, onBackClick)
        },
        error = { message ->
            StatusBarIconsEffect(lightIcons = true)
            DetailLoadState(message, retryLabel, backLabel, onRetryClick, onBackClick)
        }
    ) {
        LocalFindDetailScreenContent(
            state = state,
            audioMode = audioMode,
            onBackClick = onBackClick,
            onListenClick = {
                audioMode?.let { audio.onNavAudioTap(it, ownerKey) { showSnackBar(audioNotDownloaded) } }
            },
            onMapClick = { lat, lng, title -> viewModel.onEvent(LocalFindDetailEvent.OnMapClick(lat, lng, title)) },
            onNextStoryClick = { viewModel.onEvent(LocalFindDetailEvent.OnNextStoryClick(it)) },
            onUserScrolled = { viewModel.onEvent(LocalFindDetailEvent.OnUserScrolled) }
        )
    }
}
