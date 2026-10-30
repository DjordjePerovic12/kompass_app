package llc.bokadev.kompass.presentation.screens.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.presentation.shared.perform
import llc.bokadev.kompass.presentation.shared.rememberExternalLauncher
import llc.bokadev.kompass.presentation.shared.rememberLiveCurrentLocation
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ServiceDetailScreen(id: String) {
    val vm: ServiceDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()
    val snackbar = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    val strings = rememberAppStrings()
    val currentLocation = rememberLiveCurrentLocation(koinInject<UserLocationProvider>(), 5_000L, state.service?.id)

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is ServiceDetailEvent.Launch -> launcher.perform(event.launch, snackbar, strings)
            }
        }
    }

    ServiceDetailScreenContent(state = state, currentLocation = currentLocation, onAction = vm::onAction)
}
