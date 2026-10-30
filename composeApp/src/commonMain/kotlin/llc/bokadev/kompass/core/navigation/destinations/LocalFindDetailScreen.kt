package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.screens.localfinds.LocalFindDetailScreen

fun NavGraphBuilder.localFindDetailScreenComposable() {
    composable<Screen.LocalFindDetailScreen> { entry ->
        val route = entry.toRoute<Screen.LocalFindDetailScreen>()
        LocalFindDetailScreen(id = route.id, filter = route.filter)
    }
}
