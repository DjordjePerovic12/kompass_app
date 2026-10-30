package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.events.EventsScreen
import llc.bokadev.kompass.presentation.events.EventsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.eventsScreenComposable() {
    composable<Screen.EventsScreen> {
        val viewModel = koinViewModel<EventsViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        EventsScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
