package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.placedetail.PlaceDetailScreen
import llc.bokadev.kompass.presentation.placedetail.PlaceDetailViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.placeDetailScreenComposable() {
    composable<Screen.PlaceDetailScreen> { entry ->
        val route = entry.toRoute<Screen.PlaceDetailScreen>()
        val viewModel = koinViewModel<PlaceDetailViewModel> { parametersOf(route.id) }
        val snackbarBus = koinInject<SnackbarBus>()
        PlaceDetailScreen(
            viewModel = viewModel,
            autoplay = route.autoplay,
            showSnackBar = snackbarBus::show
        )
    }
}
