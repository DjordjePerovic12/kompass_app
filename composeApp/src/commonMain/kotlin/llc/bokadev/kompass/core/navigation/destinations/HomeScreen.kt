package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.screens.home.HomeScreen

fun NavGraphBuilder.homeScreenComposable() {
    composable<Screen.HomeScreen> {
        HomeScreen()
    }
}
