package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.infocenter.InfoCenterScreen
import llc.bokadev.kompass.presentation.infocenter.InfoCenterViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.infoCenterScreenComposable() {
    composable<Screen.InfoCenterScreen> {
        val viewModel = koinViewModel<InfoCenterViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        InfoCenterScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
