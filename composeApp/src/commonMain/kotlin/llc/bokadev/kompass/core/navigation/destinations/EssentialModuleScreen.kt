package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.essentials.EssentialsScreen
import llc.bokadev.kompass.presentation.essentials.EssentialsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.essentialModuleScreenComposable() {
    composable<Screen.EssentialModuleScreen> { entry ->
        val route = entry.toRoute<Screen.EssentialModuleScreen>()
        // The ViewModel reads "id" from its SavedStateHandle.
        val viewModel = koinViewModel<EssentialsViewModel>(key = "essentials-module-${route.id}")
        val snackbarBus = koinInject<SnackbarBus>()
        EssentialsScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
