package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.essentials.utility_map.UtilityMapScreen
import llc.bokadev.kompass.presentation.essentials.utility_map.UtilityMapViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.utilityMapScreenComposable() {
    composable<Screen.UtilityMapScreen> { entry ->
        val route = entry.toRoute<Screen.UtilityMapScreen>()
        // The ViewModel reads "category" and "selectedId" from its SavedStateHandle.
        val viewModel = koinViewModel<UtilityMapViewModel>(key = "utility-map-${route.category}-${route.selectedId}")
        val snackbarBus = koinInject<SnackbarBus>()
        UtilityMapScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
