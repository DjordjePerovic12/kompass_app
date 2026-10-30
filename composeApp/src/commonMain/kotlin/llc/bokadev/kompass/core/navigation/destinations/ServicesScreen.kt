package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.services.ServicesScreen
import llc.bokadev.kompass.presentation.services.ServicesViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.servicesScreenComposable() {
    composable<Screen.ServicesScreen> {
        val viewModel = koinViewModel<ServicesViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        ServicesScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
