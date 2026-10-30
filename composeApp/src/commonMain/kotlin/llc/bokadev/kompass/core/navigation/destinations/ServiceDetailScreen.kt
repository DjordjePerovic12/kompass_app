package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.screens.services.ServiceDetailScreen

fun NavGraphBuilder.serviceDetailScreenComposable() {
    composable<Screen.ServiceDetailScreen> { entry ->
        val route = entry.toRoute<Screen.ServiceDetailScreen>()
        ServiceDetailScreen(id = route.id)
    }
}
