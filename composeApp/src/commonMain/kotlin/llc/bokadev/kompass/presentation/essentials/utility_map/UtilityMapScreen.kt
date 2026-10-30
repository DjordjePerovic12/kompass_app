package llc.bokadev.kompass.presentation.essentials.utility_map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import org.koin.compose.koinInject

/** Orchestrator: ViewModel, state and the Maps hand-off the ViewModel asks for. */
@Composable
fun UtilityMapScreen(
    modifier: Modifier = Modifier,
    viewModel: UtilityMapViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.utilityMapState.collectAsStateWithLifecycle()
    val snackbarBus = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is UtilityMapEvent.OpenExternal) {
            launcher.perform(event.launch, snackbarBus)
        }
    }

    val content: @Composable () -> Unit = {
        UtilityMapScreenContent(
            state = state,
            onBackClick = { viewModel.onEvent(UtilityMapEvent.NavigateBack) },
            onRetryClick = { viewModel.onEvent(UtilityMapEvent.OnRetryClick) },
            onUtilitySelect = { viewModel.onEvent(UtilityMapEvent.OnUtilitySelect(it)) },
            onNavigateClick = { viewModel.onEvent(UtilityMapEvent.OnNavigateClick(it)) }
        )
    }

    // The map's sheet shows progress and the load failure inline under the top bar,
    // so loading and error render the same content as the loaded state.
    BaseContentView(
        modifier = modifier,
        viewState = state,
        loading = content,
        error = { content() }
    ) {
        content()
    }
}
