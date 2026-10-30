package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.listen.ListenScreen
import llc.bokadev.kompass.presentation.listen.ListenViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.listenScreenComposable() {
    composable<Screen.ListenScreen> {
        val viewModel = koinViewModel<ListenViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        ListenScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
