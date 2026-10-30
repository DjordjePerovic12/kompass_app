package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.explore.ExploreScreen
import llc.bokadev.kompass.presentation.explore.ExploreViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.exploreScreenComposable() {
    composable<Screen.ExploreScreen> {
        val viewModel = koinViewModel<ExploreViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        ExploreScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
