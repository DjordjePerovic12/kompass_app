package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.categorylist.CategoryListScreen
import llc.bokadev.kompass.presentation.categorylist.CategoryListViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.categoryListScreenComposable() {
    composable<Screen.CategoryListScreen> { entry ->
        val route = entry.toRoute<Screen.CategoryListScreen>()
        // Keyed by kind: each Explore list keeps its own ViewModel.
        val viewModel = koinViewModel<CategoryListViewModel>(key = "category-list-${route.kind}") { parametersOf(route.kind) }
        val snackbarBus = koinInject<SnackbarBus>()
        CategoryListScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
