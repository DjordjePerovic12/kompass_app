package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.services.detail.ServiceDetailScreen
import llc.bokadev.kompass.presentation.services.detail.ServiceDetailViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.serviceDetailScreenComposable() {
    composable<Screen.ServiceDetailScreen> { entry ->
        val route = entry.toRoute<Screen.ServiceDetailScreen>()
        val viewModel = koinViewModel<ServiceDetailViewModel> { parametersOf(route.id) }
        val snackbarBus = koinInject<SnackbarBus>()
        ServiceDetailScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
