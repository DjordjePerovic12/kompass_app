package llc.bokadev.kompass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.presentation.screens.eventdetail.EventDetailScreen
import llc.bokadev.kompass.presentation.screens.events.EventsScreen
import llc.bokadev.kompass.presentation.screens.experiencedetail.ExperienceDetailScreen
import llc.bokadev.kompass.presentation.screens.experiencedetail.ExperienceGuideScreen
import llc.bokadev.kompass.presentation.screens.experiences.ExperiencesScreen
import llc.bokadev.kompass.presentation.screens.favorites.FavoritesScreen
import llc.bokadev.kompass.presentation.screens.infocenter.InfoCenterDetailScreen
import llc.bokadev.kompass.presentation.screens.infocenter.InfoCenterScreen
import llc.bokadev.kompass.presentation.screens.itinerarydetail.ItineraryDetailScreen
import llc.bokadev.kompass.presentation.screens.languagepicker.LanguagePickerScreen
import llc.bokadev.kompass.presentation.screens.mustsee.MustSeePlacesScreen
import llc.bokadev.kompass.presentation.screens.myguides.MyGuidesScreen
import llc.bokadev.kompass.presentation.screens.placedetail.PlaceDetailScreen
import llc.bokadev.kompass.presentation.screens.placedetail.PlaceGuideScreen
import llc.bokadev.kompass.presentation.screens.category_items_list.CategoryItemsListScreen
import llc.bokadev.kompass.presentation.screens.essentials.EssentialsScreen
import llc.bokadev.kompass.presentation.screens.essentials.UtilityMapScreen
import llc.bokadev.kompass.presentation.screens.nearby.NearbyPlacesScreen
import llc.bokadev.kompass.presentation.screens.search.SearchScreen
import llc.bokadev.kompass.presentation.screens.payment.PaymentCheckoutScreen
import llc.bokadev.kompass.presentation.screens.premium.PremiumBundlesScreen
import llc.bokadev.kompass.presentation.screens.localfinds.LocalFindDetailScreen
import llc.bokadev.kompass.presentation.screens.localfinds.LocalFindsScreen
import llc.bokadev.kompass.presentation.screens.services.ServicesScreen
import llc.bokadev.kompass.presentation.screens.services.ServiceDetailScreen

@Composable
fun KompassNavHost(isFirstLaunch: Boolean) {
    val navController = rememberNavController()
    val startDestination: Route = if (isFirstLaunch) Route.LanguagePicker else Route.Main()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Route.LanguagePicker> {
            LanguagePickerScreen(
                onLanguageSelected = {
                    navController.navigate(Route.Main()) {
                        popUpTo<Route.LanguagePicker> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.ChangeLanguage> {
            LanguagePickerScreen(
                showBack = true,
                onBack = { navController.popBackStack() },
                onLanguageSelected = { navController.popBackStack() }
            )
        }

        composable<Route.Main> { backStackEntry ->
            val route: Route.Main = backStackEntry.toRoute()
            val navigateToRootTab: (BottomTab) -> Unit = { tab ->
                navController.navigate(Route.Main(tab.name)) {
                    popUpTo<Route.Main> { inclusive = true }
                    launchSingleTop = true
                }
            }
            val navigateToRootTabWithBackTarget: (BottomTab, BottomTab?) -> Unit = { tab, backTab ->
                navController.navigate(Route.Main(tab.name, backTab?.name)) {
                    popUpTo<Route.Main> { inclusive = true }
                    launchSingleTop = true
                }
            }
            MainShell(
                initialTab = runCatching { BottomTab.valueOf(route.tab) }.getOrElse { BottomTab.Home },
                backTab = route.backTab?.let { runCatching { BottomTab.valueOf(it) }.getOrNull() },
                onSelectRootTab = navigateToRootTab,
                onSelectRootTabWithBackTarget = navigateToRootTabWithBackTarget,
                onNavigateToPlaceDetail = { id -> navController.navigate(Route.PlaceDetail(id)) },
                onNavigateToEventDetail = { id -> navController.navigate(Route.EventDetail(id)) },
                onNavigateToEvents = { navController.navigate(Route.Events) },
                onNavigateToExperienceDetail = { id -> navController.navigate(Route.ExperienceDetail(id)) },
                onNavigateToInfoCenterDetail = { id -> navController.navigate(Route.InfoCenterDetail(id)) },
                onNavigateToItineraryDetail = { id -> navController.navigate(Route.ItineraryDetail(id)) },
                onNavigateToPlacesList = { category -> navController.navigate(Route.CategoryItemsList(category)) },
                onNavigateToMustSeePlaces = { navController.navigate(Route.MustSeePlaces) },
                onNavigateToNearbyPlaces = { navController.navigate(Route.NearbyPlaces) },
                onNavigateToServices = { navController.navigate(Route.Services) },
                onNavigateToInfoCenter = { navController.navigate(Route.InfoCenter) },
                onNavigateToMyGuides = { navController.navigate(Route.MyGuides) },
                onNavigateToFavorites = { navController.navigate(Route.Favorites) },
                onNavigateToSearch = { navController.navigate(Route.Search) },
                onNavigateToChangeLanguage = { navController.navigate(Route.ChangeLanguage) },
                onNavigateToLocalFinds = { navController.navigate(Route.LocalFinds) },
                onNavigateToUtilityMap = { category ->
                    navController.navigate(Route.UtilityMap(category.name))
                }
            )
        }

        composable<Route.Events> {
            EventsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToEventDetail = { id -> navController.navigate(Route.EventDetail(id)) }
            )
        }

        composable<Route.PlaceDetail> { backStackEntry ->
            val route: Route.PlaceDetail = backStackEntry.toRoute()
            PlaceDetailScreen(
                id = route.id,
                onBack = { navController.popBackStack() },
                onLearnMore = { navController.navigate(Route.PremiumBundles()) }
            )
        }

        composable<Route.PlaceGuide> { backStackEntry ->
            val route: Route.PlaceGuide = backStackEntry.toRoute()
            PlaceGuideScreen(
                id = route.id,
                autoplay = route.autoplay,
                deep = route.deep,
                onBack = { navController.popBackStack() }
            )
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
                onLearnMore = { navController.navigate(Route.PremiumBundles(route.id)) }
            )
        }

        composable<Route.ExperienceGuide> { backStackEntry ->
            val route: Route.ExperienceGuide = backStackEntry.toRoute()
            ExperienceGuideScreen(
                id = route.id,
                autoplay = route.autoplay,
                deep = route.deep,
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

        composable<Route.MustSeePlaces> {
            MustSeePlacesScreen(
                onBack = { navController.popBackStack() },
                onPlaceClick = { id -> navController.navigate(Route.PlaceDetail(id)) }
            )
        }

        composable<Route.Activities> {
            LaunchedEffect(Unit) {
                navController.navigate(Route.Main(BottomTab.Activities.name)) {
                    popUpTo<Route.Main> { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable<Route.Essentials> {
            LaunchedEffect(Unit) {
                navController.navigate(Route.Main(BottomTab.Essentials.name)) {
                    popUpTo<Route.Main> { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable<Route.UtilityMap> { backStackEntry ->
            val route: Route.UtilityMap = backStackEntry.toRoute()
            UtilityMapScreen(
                vmKey = "utility-map-${route.category}",
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.Services> {
            ServicesScreen(
                onBack = { navController.popBackStack() },
                onServiceClick = { id -> navController.navigate(Route.ServiceDetail(id)) }
            )
        }

        composable<Route.ServiceDetail> { backStackEntry ->
            val route: Route.ServiceDetail = backStackEntry.toRoute()
            ServiceDetailScreen(
                id = route.id,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.MyGuides> {
            MyGuidesScreen(
                onBack = { navController.popBackStack() },
                onOpenPlaceGuide = { id, deep -> navController.navigate(Route.PlaceGuide(id, autoplay = true, deep = deep)) },
                onOpenActivityGuide = { id, deep -> navController.navigate(Route.ExperienceGuide(id, autoplay = true, deep = deep)) },
                onOpenPlace = { id -> navController.navigate(Route.PlaceDetail(id)) },
                onOpenActivity = { id -> navController.navigate(Route.ExperienceDetail(id)) }
            )
        }

        composable<Route.Favorites> {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onPlaceClick = { id -> navController.navigate(Route.PlaceDetail(id)) },
                onActivityClick = { id -> navController.navigate(Route.ExperienceDetail(id)) }
            )
        }

        composable<Route.Search> {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onPlaceClick = { id -> navController.navigate(Route.PlaceDetail(id)) },
                onActivityClick = { id -> navController.navigate(Route.ExperienceDetail(id)) },
                onEventClick = { id -> navController.navigate(Route.EventDetail(id)) }
            )
        }

        composable<Route.InfoCenter> {
            InfoCenterScreen(
                onBack = { navController.popBackStack() },
                onNoticeClick = { id -> navController.navigate(Route.InfoCenterDetail(id)) }
            )
        }

        composable<Route.InfoCenterDetail> { backStackEntry ->
            val route: Route.InfoCenterDetail = backStackEntry.toRoute()
            InfoCenterDetailScreen(
                id = route.id,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.PremiumBundles> { backStackEntry ->
            val route: Route.PremiumBundles = backStackEntry.toRoute()
            PremiumBundlesScreen(
                onBack = { navController.popBackStack() },
                unlockTargetActivityId = route.activityId,
                onNavigateToGuide = { id ->
                    navController.navigate(Route.ExperienceGuide(id, deep = true)) {
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

        composable<Route.LocalFinds> {
            LocalFindsScreen(
                onBack = { navController.popBackStack() },
                onFindClick = { id -> navController.navigate(Route.LocalFindDetail(id)) }
            )
        }

        composable<Route.LocalFindDetail> { backStackEntry ->
            val route: Route.LocalFindDetail = backStackEntry.toRoute()
            LocalFindDetailScreen(
                id = route.id,
                onBack = { navController.popBackStack() },
                onLearnMore = { navController.navigate(Route.PremiumBundles()) }
            )
        }
    }
}
