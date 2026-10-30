package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.essentials.EssentialsScreen
import llc.bokadev.kompass.presentation.essentials.EssentialsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.essentialsScreenComposable() {
    composable<Screen.EssentialsScreen> {
        val viewModel = koinViewModel<EssentialsViewModel>(key = "essentials")
        val snackbarBus = koinInject<SnackbarBus>()
        EssentialsScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
