package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.localfinds.detail.LocalFindDetailScreen
import llc.bokadev.kompass.presentation.localfinds.detail.LocalFindDetailViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.localFindDetailScreenComposable() {
    composable<Screen.LocalFindDetailScreen> { entry ->
        val route = entry.toRoute<Screen.LocalFindDetailScreen>()
        val viewModel = koinViewModel<LocalFindDetailViewModel>(key = "story-${route.id}") { parametersOf(route.id, route.filter) }
        val snackbarBus = koinInject<SnackbarBus>()
        LocalFindDetailScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
