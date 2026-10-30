package llc.bokadev.kompass.presentation.screens.eventdetail

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

/** Orchestrator: ViewModel, state, live location, and the external hand-offs the ViewModel asks for. */
@Composable
fun EventDetailScreen(id: String) {
    val vm: EventDetailViewModel = koinViewModel(parameters = { parametersOf(id) })
    val state by vm.state.collectAsState()
    val snackbar = koinInject<SnackbarBus>()
    val launcher = rememberExternalLauncher()
    val strings = rememberAppStrings()
    val currentLocation = rememberLiveCurrentLocation(koinInject<UserLocationProvider>(), 5_000L, state.event?.id)

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is EventDetailEvent.Launch -> launcher.perform(event.launch, snackbar, strings)
            }
        }
    }

    EventDetailScreenContent(state = state, currentLocation = currentLocation, onAction = vm::onAction)
}
