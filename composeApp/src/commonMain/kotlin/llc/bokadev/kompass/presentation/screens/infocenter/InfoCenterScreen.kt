package llc.bokadev.kompass.presentation.screens.infocenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

/** "Local updates": town notices, disruptions and practical updates. Orchestrator only. */
@Composable
fun InfoCenterScreen(vmKey: String = "info-center") {
    val vm: InfoCenterViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    InfoCenterScreenContent(state = state, onAction = vm::onAction)
}
