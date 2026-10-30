package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.screens.infocenter.InfoCenterDetailScreen

fun NavGraphBuilder.infoCenterDetailScreenComposable() {
    composable<Screen.InfoCenterDetailScreen> { entry ->
        val route = entry.toRoute<Screen.InfoCenterDetailScreen>()
        InfoCenterDetailScreen(id = route.id)
    }
}
