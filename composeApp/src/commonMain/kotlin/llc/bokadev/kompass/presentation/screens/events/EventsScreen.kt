package llc.bokadev.kompass.presentation.screens.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

/** Orchestrator only: the ViewModel navigates through its Navigator; no callbacks here. */
@Composable
fun EventsScreen(vmKey: String = "events") {
    val vm: EventsViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    EventsScreenContent(state = state, onAction = vm::onAction)
}
