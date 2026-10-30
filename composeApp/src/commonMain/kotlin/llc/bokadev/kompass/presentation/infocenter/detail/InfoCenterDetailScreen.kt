package llc.bokadev.kompass.presentation.infocenter.detail

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
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.detail.DetailLoadState
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun InfoCenterDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: InfoCenterDetailViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.infoCenterDetailState.collectAsStateWithLifecycle()
    val snackbarBus = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    // The shared launcher still takes the AppStrings tables for its failure snackbars.
    val strings = appStringsFor(LocalAppLocale.current.substringBefore('-'))

    StatusBarIconsEffect(lightIcons = true)

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is InfoCenterDetailEvent.OpenExternal) {
            launcher.perform(event.launch, snackbarBus, strings)
        }
    }

    val retryLabel = stringResource(Res.string.retry)
    val backLabel = stringResource(Res.string.detail_back)
    val onRetryClick = { viewModel.onEvent(InfoCenterDetailEvent.OnRetryClick) }
    val onBackClick = { viewModel.onEvent(InfoCenterDetailEvent.NavigateBack) }

    BaseContentView(
        modifier = modifier.pageGradientBackground(),
        viewState = state,
        loading = { DetailLoadState(null, retryLabel, backLabel, onRetryClick, onBackClick) },
        error = { message -> DetailLoadState(message, retryLabel, backLabel, onRetryClick, onBackClick) }
    ) {
        InfoCenterDetailScreenContent(
            state = state,
            onBackClick = onBackClick,
            onSourceClick = { viewModel.onEvent(InfoCenterDetailEvent.OnSourceClick) }
        )
    }
}
