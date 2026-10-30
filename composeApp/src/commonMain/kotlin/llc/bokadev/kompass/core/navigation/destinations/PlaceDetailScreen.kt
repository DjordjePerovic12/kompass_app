package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.screens.placedetail.PlaceDetailScreen

fun NavGraphBuilder.placeDetailScreenComposable() {
    composable<Screen.PlaceDetailScreen> { entry ->
        val route = entry.toRoute<Screen.PlaceDetailScreen>()
        PlaceDetailScreen(id = route.id, autoplay = route.autoplay)
    }
}
