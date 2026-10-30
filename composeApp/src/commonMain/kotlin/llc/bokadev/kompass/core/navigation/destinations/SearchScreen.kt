package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.search.SearchScreen
import llc.bokadev.kompass.presentation.search.SearchViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.searchScreenComposable() {
    composable<Screen.SearchScreen> {
        val viewModel = koinViewModel<SearchViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        SearchScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
