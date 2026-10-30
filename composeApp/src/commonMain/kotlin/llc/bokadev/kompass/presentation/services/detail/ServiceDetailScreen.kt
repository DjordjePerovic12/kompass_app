package llc.bokadev.kompass.presentation.services.detail

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.retry
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailLoadState
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import llc.bokadev.kompass.presentation.shared.rememberLiveCurrentLocation
import llc.bokadev.kompass.ui.theme.KompassTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ServiceDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ServiceDetailViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.serviceDetailState.collectAsStateWithLifecycle()
    val snackbarBus = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    // The shared launcher still takes the AppStrings tables for its failure snackbars.
    val strings = appStringsFor(LocalAppLocale.current.substringBefore('-'))
    val currentLocation = rememberLiveCurrentLocation(koinInject<UserLocationProvider>(), 5_000L, state.service?.id)

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is ServiceDetailEvent.OpenExternal) {
            launcher.perform(event.launch, snackbarBus, strings)
        }
    }

    val retryLabel = stringResource(Res.string.retry)
    val backLabel = stringResource(Res.string.detail_back)
    val onRetryClick = { viewModel.onEvent(ServiceDetailEvent.OnRetryClick) }
    val onBackClick = { viewModel.onEvent(ServiceDetailEvent.NavigateBack) }

    BaseContentView(
        modifier = modifier.background(KompassTheme.colors.nightBlack),
        viewState = state,
        loading = { DetailLoadState(null, retryLabel, backLabel, onRetryClick, onBackClick) },
        error = { message -> DetailLoadState(message, retryLabel, backLabel, onRetryClick, onBackClick) }
    ) {
        ServiceDetailScreenContent(
            state = state,
            currentLocation = currentLocation,
            onBackClick = onBackClick,
            onWebsiteClick = { viewModel.onEvent(ServiceDetailEvent.OnWebsiteClick) },
            onMapsClick = { viewModel.onEvent(ServiceDetailEvent.OnMapsClick) }
        )
    }
}
