package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.screens.essentials.UtilityMapScreen

fun NavGraphBuilder.utilityMapScreenComposable() {
    composable<Screen.UtilityMapScreen> { entry ->
        val route = entry.toRoute<Screen.UtilityMapScreen>()
        UtilityMapScreen(vmKey = "utility-map-${route.category}-${route.selectedId}")
    }
}
