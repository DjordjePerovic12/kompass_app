package llc.bokadev.kompass.presentation.screens.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.core.presentation.SnackbarMessage
import llc.bokadev.kompass.core.utils.rememberAppStrings
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** Saved orchestrator: ViewModel, state, and the "Removed · Undo" confirmation. */
@Composable
fun FavoritesScreen() {
    val vm: FavoritesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val snackbar = koinInject<SnackbarBus>()
    val strings = rememberAppStrings()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is FavoritesEvent.Removed -> snackbar.show(
                    SnackbarMessage(
                        text = strings.saved.removed.replace("{title}", event.title),
                        actionLabel = strings.saved.undo,
                        onAction = { vm.onAction(FavoritesAction.Undo(event.entry)) }
                    )
                )
            }
        }
    }

    FavoritesScreenContent(state = state, onAction = vm::onAction)
}
