package llc.bokadev.kompass.presentation.screens.essentials

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.presentation.shared.ExternalLaunch
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UtilityMapScreen(vmKey: String = "utility-map") {
    val vm: UtilityMapViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val snackbar = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    val strings = rememberAppStrings()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is UtilityMapEvent.Launch -> launcher.perform(event.launch, snackbar, strings)
            }
        }
    }

    UtilityMapScreenContent(state = state, onAction = vm::onAction)
}
