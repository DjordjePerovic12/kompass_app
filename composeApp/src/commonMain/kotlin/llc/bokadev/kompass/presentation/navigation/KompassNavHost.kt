package llc.bokadev.kompass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import llc.bokadev.kompass.presentation.screens.eventdetail.EventDetailScreen
import llc.bokadev.kompass.presentation.screens.experiencedetail.ExperienceDetailScreen
import llc.bokadev.kompass.presentation.screens.itinerarydetail.ItineraryDetailScreen
import llc.bokadev.kompass.presentation.screens.languagepicker.LanguagePickerScreen
import llc.bokadev.kompass.presentation.screens.placedetail.PlaceDetailScreen

@Composable
fun KompassNavHost(isFirstLaunch: Boolean) {
    val navController = rememberNavController()
    val startDestination: Route = if (isFirstLaunch) Route.LanguagePicker else Route.Main

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Route.LanguagePicker> {
            LanguagePickerScreen(
                onLanguageSelected = {
                    navController.navigate(Route.Main) {
                        popUpTo<Route.LanguagePicker> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Main> {
            MainShell(
                onNavigateToPlaceDetail = { id -> navController.navigate(Route.PlaceDetail(id)) },
                onNavigateToEventDetail = { id -> navController.navigate(Route.EventDetail(id)) },
                onNavigateToExperienceDetail = { id -> navController.navigate(Route.ExperienceDetail(id)) },
                onNavigateToItineraryDetail = { id -> navController.navigate(Route.ItineraryDetail(id)) }
            )
        }

        composable<Route.PlaceDetail> { backStackEntry ->
            val route: Route.PlaceDetail = backStackEntry.toRoute()
            PlaceDetailScreen(id = route.id, onBack = { navController.popBackStack() })
        }

        composable<Route.EventDetail> { backStackEntry ->
            val route: Route.EventDetail = backStackEntry.toRoute()
            EventDetailScreen(id = route.id, onBack = { navController.popBackStack() })
        }

        composable<Route.ExperienceDetail> { backStackEntry ->
            val route: Route.ExperienceDetail = backStackEntry.toRoute()
            ExperienceDetailScreen(id = route.id, onBack = { navController.popBackStack() })
        }

        composable<Route.ItineraryDetail> { backStackEntry ->
            val route: Route.ItineraryDetail = backStackEntry.toRoute()
            ItineraryDetailScreen(id = route.id, onBack = { navController.popBackStack() })
        }
    }
}
