package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.screens.eventdetail.EventDetailScreen

fun NavGraphBuilder.eventDetailScreenComposable() {
    composable<Screen.EventDetailScreen> { entry ->
        val route = entry.toRoute<Screen.EventDetailScreen>()
        EventDetailScreen(id = route.id)
    }
}
