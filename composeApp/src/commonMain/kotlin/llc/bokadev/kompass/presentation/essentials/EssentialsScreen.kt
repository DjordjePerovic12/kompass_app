package llc.bokadev.kompass.presentation.essentials

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.observeWithLifecycle
import llc.bokadev.kompass.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import org.koin.compose.koinInject

/**
 * Orchestrator: ViewModel, state, connectivity and the external hand-offs (dialer, maps, browser,
 * settings) the ViewModel asks for. Screen.EssentialModuleScreen(id) opens the same screen focused on one module.
 */
@Composable
fun EssentialsScreen(
    modifier: Modifier = Modifier,
    viewModel: EssentialsViewModel,
    showSnackBar: (String) -> Unit
) {
    val state by viewModel.essentialsState.collectAsStateWithLifecycle()
    val online by koinInject<ConnectivityMonitor>().isOnline.collectAsStateWithLifecycle()
    val snackbarBus = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    // perform() still takes the AppStrings tables until it is migrated.
    val strings = appStringsFor(LocalAppLocale.current.substringBefore('-'))

    viewModel.snackBarChannel.observeWithLifecycle { message ->
        showSnackBar(message)
    }

    viewModel.eventFlow.observeWithLifecycle { event ->
        if (event is EssentialsEvent.OpenExternal) {
            launcher.perform(event.launch, snackbarBus, strings)
        }
    }

    val content: @Composable () -> Unit = {
        EssentialsScreenContent(
            state = state,
            offline = !online,
            onLanguageClick = { viewModel.onEvent(EssentialsEvent.OnLanguageClick) },
            onTabSelect = { viewModel.onEvent(EssentialsEvent.OnTabSelect(it)) },
            onBackClick = { viewModel.onEvent(EssentialsEvent.NavigateBack) },
            onItemToggle = { viewModel.onEvent(EssentialsEvent.OnItemToggle(it)) },
            onItemExpand = { viewModel.onEvent(EssentialsEvent.OnItemExpand(it)) },
            onHideReminderClick = { viewModel.onEvent(EssentialsEvent.OnHideReminderClick) },
            onRetryClick = { viewModel.onEvent(EssentialsEvent.OnRetryClick) },
            onUtilityMapClick = { category, selectedId -> viewModel.onEvent(EssentialsEvent.OnUtilityMapClick(category, selectedId)) },
            onDialClick = { viewModel.onEvent(EssentialsEvent.OnDialClick(it)) },
            onLocationClick = { title, lat, lng -> viewModel.onEvent(EssentialsEvent.OnLocationClick(title, lat, lng)) },
            onLinkClick = { viewModel.onEvent(EssentialsEvent.OnLinkClick(it)) },
            onTurnOnLocationClick = { viewModel.onEvent(EssentialsEvent.OnTurnOnLocationClick) }
        )
    }

    // The header, emergency numbers and each part's own failure show inline at every stage,
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
