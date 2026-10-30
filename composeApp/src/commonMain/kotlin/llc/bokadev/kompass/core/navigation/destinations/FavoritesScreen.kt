package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.favorites.FavoritesScreen
import llc.bokadev.kompass.presentation.favorites.FavoritesViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.favoritesScreenComposable() {
    composable<Screen.FavoritesScreen> {
        val viewModel = koinViewModel<FavoritesViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        FavoritesScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
