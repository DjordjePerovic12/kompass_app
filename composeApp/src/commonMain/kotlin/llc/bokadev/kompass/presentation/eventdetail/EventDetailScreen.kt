package llc.bokadev.kompass.presentation.eventdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.retry
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.detail.DetailLoadState
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import llc.bokadev.kompass.presentation.shared.rememberLiveCurrentLocation
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun EventDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: EventDetailViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.eventDetailState.collectAsStateWithLifecycle()
    val snackbarBus = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    val strings = rememberAppStrings()
    val currentLocation = rememberLiveCurrentLocation(koinInject<UserLocationProvider>(), 5_000L, state.event?.id)

    StatusBarIconsEffect(lightIcons = true)

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is EventDetailEvent.OpenExternal) {
            launcher.perform(event.launch, snackbarBus, strings)
        }
    }

    val retryLabel = stringResource(Res.string.retry)
    val backLabel = stringResource(Res.string.detail_back)
    val onRetryClick = { viewModel.onEvent(EventDetailEvent.OnRetryClick) }
    val onBackClick = { viewModel.onEvent(EventDetailEvent.NavigateBack) }

    BaseContentView(
        modifier = modifier.pageGradientBackground(),
        viewState = state,
        loading = { DetailLoadState(null, retryLabel, backLabel, onRetryClick, onBackClick) },
        error = { message -> DetailLoadState(message, retryLabel, backLabel, onRetryClick, onBackClick) }
    ) {
        EventDetailScreenContent(
            state = state,
            currentLocation = currentLocation,
            onBackClick = onBackClick,
            onTicketsClick = { viewModel.onEvent(EventDetailEvent.OnTicketsClick) },
            onMapsClick = { viewModel.onEvent(EventDetailEvent.OnMapsClick) }
        )
    }
}
