package llc.bokadev.kompass.presentation.screens.excursions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import org.koin.compose.viewmodel.koinViewModel

/** Orchestrator only; the ViewModel navigates and saves. */
@Composable
fun ExcursionsScreen() {
    val vm: ExcursionsViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    StatusBarIconsEffect(lightIcons = true)
    ExcursionsScreenContent(state = state, onAction = vm::onAction)
}
