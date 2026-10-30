package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.infocenter.detail.InfoCenterDetailScreen
import llc.bokadev.kompass.presentation.infocenter.detail.InfoCenterDetailViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.infoCenterDetailScreenComposable() {
    composable<Screen.InfoCenterDetailScreen> { entry ->
        val route = entry.toRoute<Screen.InfoCenterDetailScreen>()
        val viewModel = koinViewModel<InfoCenterDetailViewModel> { parametersOf(route.id) }
        val snackbarBus = koinInject<SnackbarBus>()
        InfoCenterDetailScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
