package llc.bokadev.kompass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import llc.bokadev.kompass.presentation.screens.eventdetail.EventDetailScreen
import llc.bokadev.kompass.presentation.screens.experiencedetail.ExperienceDetailScreen
import llc.bokadev.kompass.presentation.screens.experiencedetail.ExperienceGuideScreen
import llc.bokadev.kompass.presentation.screens.experiences.ExperiencesScreen
import llc.bokadev.kompass.presentation.screens.itinerarydetail.ItineraryDetailScreen
import llc.bokadev.kompass.presentation.screens.languagepicker.LanguagePickerScreen
import llc.bokadev.kompass.presentation.screens.placedetail.PlaceDetailScreen
import llc.bokadev.kompass.presentation.screens.category_items_list.CategoryItemsListScreen
import llc.bokadev.kompass.presentation.screens.essentials.EssentialsScreen
import llc.bokadev.kompass.presentation.screens.nearby.NearbyPlacesScreen
import llc.bokadev.kompass.presentation.screens.payment.PaymentCheckoutScreen
import llc.bokadev.kompass.presentation.screens.premium.PremiumBundlesScreen
import llc.bokadev.kompass.presentation.screens.services.ServicesScreen

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
                onNavigateToItineraryDetail = { id -> navController.navigate(Route.ItineraryDetail(id)) },
                onNavigateToPlacesList = { category -> navController.navigate(Route.CategoryItemsList(category)) },
                onNavigateToActivities = { navController.navigate(Route.Activities) },
                onNavigateToNearbyPlaces = { navController.navigate(Route.NearbyPlaces) },
                onNavigateToEssentials = { navController.navigate(Route.Essentials) },
                onNavigateToServices = { navController.navigate(Route.Services) }
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
            ExperienceDetailScreen(
                id = route.id,
                onBack = { navController.popBackStack() },
                onLearnMore = { navController.navigate(Route.PremiumBundles(route.id)) },
                onOpenGuide = { id -> navController.navigate(Route.ExperienceGuide(id)) }
            )
        }

        composable<Route.ExperienceGuide> { backStackEntry ->
            val route: Route.ExperienceGuide = backStackEntry.toRoute()
            ExperienceGuideScreen(
                id = route.id,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.ItineraryDetail> { backStackEntry ->
            val route: Route.ItineraryDetail = backStackEntry.toRoute()
            ItineraryDetailScreen(id = route.id, onBack = { navController.popBackStack() })
        }

        composable<Route.CategoryItemsList> { backStackEntry ->
            val route: Route.CategoryItemsList = backStackEntry.toRoute()
            CategoryItemsListScreen(
                category = route.category,
                onBack = { navController.popBackStack() },
                onPlaceClick = { id -> navController.navigate(Route.PlaceDetail(id)) }
            )
        }

        composable<Route.NearbyPlaces> {
            NearbyPlacesScreen(
                onBack = { navController.popBackStack() },
                onPlaceClick = { id -> navController.navigate(Route.PlaceDetail(id)) }
            )
        }

        composable<Route.Activities> {
            ExperiencesScreen(
                vmKey = "activities-route",
                onNavigateToExperienceDetail = { id -> navController.navigate(Route.ExperienceDetail(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.Essentials> {
            EssentialsScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.Services> {
            ServicesScreen(onBack = { navController.popBackStack() })
        }

        composable<Route.PremiumBundles> { backStackEntry ->
            val route: Route.PremiumBundles = backStackEntry.toRoute()
            PremiumBundlesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCheckout = { sessionId ->
                    navController.navigate(Route.PaymentCheckout(sessionId))
                },
                unlockTargetActivityId = route.activityId,
                onNavigateToGuide = { id ->
                    navController.navigate(Route.ExperienceGuide(id)) {
                        popUpTo<Route.PremiumBundles> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.PaymentCheckout> { backStackEntry ->
            val route: Route.PaymentCheckout = backStackEntry.toRoute()
            PaymentCheckoutScreen(
                sessionId = route.sessionId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
