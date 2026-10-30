package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.eventdetail.EventDetailScreen
import llc.bokadev.kompass.presentation.eventdetail.EventDetailViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.eventDetailScreenComposable() {
    composable<Screen.EventDetailScreen> { entry ->
        val route = entry.toRoute<Screen.EventDetailScreen>()
        val viewModel = koinViewModel<EventDetailViewModel> { parametersOf(route.id) }
        val snackbarBus = koinInject<SnackbarBus>()
        EventDetailScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
