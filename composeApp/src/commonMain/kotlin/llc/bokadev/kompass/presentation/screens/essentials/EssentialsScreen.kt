package llc.bokadev.kompass.presentation.screens.essentials

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.utils.ConnectivityMonitor
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Orchestrator: ViewModel, state, connectivity and the external hand-offs (dialer, maps, browser,
 * settings) the ViewModel asks for. Screen.EssentialModuleScreen(id) opens the same screen focused on one module.
 */
@Composable
fun EssentialsScreen(vmKey: String = "essentials") {
    val vm: EssentialsViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val connectivity = koinInject<ConnectivityMonitor>()
    val online by connectivity.isOnline.collectAsState()
    val snackbar = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    val strings = rememberAppStrings()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is EssentialsEvent.Launch -> launcher.perform(event.launch, snackbar, strings)
            }
        }
    }

    EssentialsScreenContent(state = state, offline = !online, onAction = vm::onAction)
}
