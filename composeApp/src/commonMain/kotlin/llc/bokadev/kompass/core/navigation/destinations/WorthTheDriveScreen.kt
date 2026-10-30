package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.excursions.ExcursionsScreen
import llc.bokadev.kompass.presentation.excursions.ExcursionsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.worthTheDriveScreenComposable() {
    composable<Screen.WorthTheDriveScreen> {
        val viewModel = koinViewModel<ExcursionsViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        ExcursionsScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
