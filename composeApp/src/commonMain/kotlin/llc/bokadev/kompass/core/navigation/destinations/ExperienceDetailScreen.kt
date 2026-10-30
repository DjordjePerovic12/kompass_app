package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.experiencedetail.ExperienceDetailScreen
import llc.bokadev.kompass.presentation.experiencedetail.ExperienceDetailViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.experienceDetailScreenComposable() {
    composable<Screen.ExperienceDetailScreen> { entry ->
        val route = entry.toRoute<Screen.ExperienceDetailScreen>()
        val viewModel = koinViewModel<ExperienceDetailViewModel> { parametersOf(route.id) }
        val snackbarBus = koinInject<SnackbarBus>()
        ExperienceDetailScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
