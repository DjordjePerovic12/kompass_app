package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.localfinds.LocalFindsScreen
import llc.bokadev.kompass.presentation.localfinds.LocalFindsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.localFindsScreenComposable() {
    composable<Screen.LocalFindsScreen> {
        val viewModel = koinViewModel<LocalFindsViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        LocalFindsScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
