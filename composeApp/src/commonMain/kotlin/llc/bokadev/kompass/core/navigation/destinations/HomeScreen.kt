package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.home.HomeScreen
import llc.bokadev.kompass.presentation.home.HomeViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.homeScreenComposable() {
    composable<Screen.HomeScreen> {
        val viewModel = koinViewModel<HomeViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        HomeScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
