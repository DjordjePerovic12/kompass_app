package llc.bokadev.kompass.presentation.screens.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

/** Orchestrator only; the ViewModel navigates. */
@Composable
fun ServicesScreen() {
    val vm: ServicesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    ServicesScreenContent(state = state, onAction = vm::onAction)
}
