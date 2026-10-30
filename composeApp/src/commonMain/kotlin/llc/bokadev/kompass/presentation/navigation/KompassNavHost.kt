package llc.bokadev.kompass.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.presentation.mvi.Navigator
import llc.bokadev.kompass.presentation.screens.excursions.ExcursionsScreen
import llc.bokadev.kompass.presentation.screens.categorylist.CategoryListKind
import llc.bokadev.kompass.presentation.screens.categorylist.CategoryListScreen
import llc.bokadev.kompass.presentation.screens.essentials.EssentialsScreen
import llc.bokadev.kompass.presentation.screens.essentials.UtilityMapScreen
import llc.bokadev.kompass.presentation.screens.eventdetail.EventDetailScreen
import llc.bokadev.kompass.presentation.screens.events.EventsScreen
import llc.bokadev.kompass.presentation.screens.experiencedetail.ExperienceDetailScreen
import llc.bokadev.kompass.presentation.screens.favorites.FavoritesScreen
import llc.bokadev.kompass.presentation.screens.infocenter.InfoCenterDetailScreen
import llc.bokadev.kompass.presentation.screens.infocenter.InfoCenterScreen
import llc.bokadev.kompass.presentation.screens.languagepicker.LanguagePickerScreen
import llc.bokadev.kompass.presentation.screens.localfinds.LocalFindDetailScreen
import llc.bokadev.kompass.presentation.screens.localfinds.LocalFindsScreen
import llc.bokadev.kompass.presentation.screens.listen.ListenScreen
import llc.bokadev.kompass.presentation.screens.placedetail.PlaceDetailScreen
import org.koin.compose.koinInject
import llc.bokadev.kompass.presentation.screens.search.SearchScreen
import llc.bokadev.kompass.presentation.screens.services.ServiceDetailScreen
import llc.bokadev.kompass.presentation.screens.services.ServicesScreen

private const val NavTransitionEnterMs = 180
private const val NavTransitionExitMs = 150

internal val kompassEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(tween(NavTransitionEnterMs)) +
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NavTransitionEnterMs))
}
internal val kompassExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(tween(NavTransitionExitMs)) +
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NavTransitionExitMs))
}
internal val kompassPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(tween(NavTransitionEnterMs)) +
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NavTransitionEnterMs))
}
internal val kompassPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(tween(NavTransitionExitMs)) +
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NavTransitionExitMs))
}

@Composable
fun KompassNavHost(isFirstLaunch: Boolean) {
    val navController = rememberNavController()
    val navigator = koinInject<Navigator>()
    NavHost(navController = navController,
        startDestination = if (isFirstLaunch) Route.LanguagePicker else Route.Main,
        enterTransition = kompassEnterTransition,
        exitTransition = kompassExitTransition,
        popEnterTransition = kompassPopEnterTransition,
        popExitTransition = kompassPopExitTransition) {
        composable<Route.LanguagePicker> {
            // Only first launch lives on this root controller; MainShell's bridge serves the tabs.
            NavigatorBridge(navigator, navController)
            LanguagePickerScreen()
        }
        composable<Route.Main> { MainShell() }
    }
}

/**
 * Destinations shared by every tab's graph. No callbacks: each screen's ViewModel navigates
 * through the injected [Navigator], applied to the selected tab's controller by MainShell.
 */
internal fun NavGraphBuilder.detailDestinations() {
    composable<Route.Events> { EventsScreen() }

    composable<Route.PlaceDetail> { entry ->
        val route: Route.PlaceDetail = entry.toRoute()
        PlaceDetailScreen(id = route.id, autoplay = route.autoplay)
    }

    // Compatibility only (restored back stacks): the legacy guide now shows the current detail.
    composable<Route.PlaceGuide> { entry ->
        val route: Route.PlaceGuide = entry.toRoute()
        PlaceDetailScreen(id = route.id, autoplay = route.autoplay)
    }

    composable<Route.EventDetail> { entry -> EventDetailScreen(id = entry.toRoute<Route.EventDetail>().id) }

    composable<Route.ExperienceDetail> { entry -> ExperienceDetailScreen(id = entry.toRoute<Route.ExperienceDetail>().id) }

    // Compatibility only: the legacy activity guide now shows the current activity detail.
    composable<Route.ExperienceGuide> { entry -> ExperienceDetailScreen(id = entry.toRoute<Route.ExperienceGuide>().id) }

    // Unfinished in the pilot: never shown, even from a restored back stack.
    composable<Route.ItineraryDetail> { ClosePilotHiddenRoute() }

    composable<Route.CategoryList> { entry ->
        val kind = entry.toRoute<Route.CategoryList>().kind
        // The old distance-based "WORTH_THE_DRIVE" list is gone; a restored stack opens the editorial collection.
        if (kind == "WORTH_THE_DRIVE") ExcursionsScreen() else CategoryListScreen(kind = kind)
    }

    // Legacy list routes (compatibility for restored back stacks) render the shared list.
    composable<Route.CategoryItemsList> { entry ->
        val category = entry.toRoute<Route.CategoryItemsList>().category
        CategoryListScreen(kind = (if (category.equals("eat_and_drink", ignoreCase = true)) CategoryListKind.EAT_AND_DRINK else CategoryListKind.PLACES).name)
    }

    composable<Route.NearbyPlaces> { entry ->
        if (entry.toRoute<Route.NearbyPlaces>().worthTheDrive) ExcursionsScreen()
        else CategoryListScreen(kind = CategoryListKind.PLACES.name)
    }

    composable<Route.MustSeePlaces> { CategoryListScreen(kind = CategoryListKind.MUST_SEE.name) }

    composable<Route.WorthTheDrive> { ExcursionsScreen() }

    composable<Route.Activities> { CategoryListScreen(kind = CategoryListKind.ACTIVITIES.name) }

    composable<Route.Essentials> { EssentialsScreen() }

    // Route.EssentialModule(id): the ViewModel reads "id" from its SavedStateHandle.
    composable<Route.EssentialModule> { entry ->
        EssentialsScreen(vmKey = "essentials-module-${entry.toRoute<Route.EssentialModule>().id}")
    }

    composable<Route.UtilityMap> { entry ->
        val route: Route.UtilityMap = entry.toRoute()
        UtilityMapScreen(vmKey = "utility-map-${route.category}-${route.selectedId}")
    }

    composable<Route.Services> { ServicesScreen() }

    composable<Route.ServiceDetail> { entry -> ServiceDetailScreen(id = entry.toRoute<Route.ServiceDetail>().id) }

    // The Lamplight audio library ("Listen"); the route name is kept for existing callers.
    composable<Route.MyGuides> { ListenScreen() }

    composable<Route.Favorites> { FavoritesScreen() }

    composable<Route.Search> { SearchScreen() }

    composable<Route.InfoCenter> { InfoCenterScreen() }

    composable<Route.InfoCenterDetail> { entry -> InfoCenterDetailScreen(id = entry.toRoute<Route.InfoCenterDetail>().id) }

    // Payments are out of the pilot: never shown, even from a restored back stack.
    composable<Route.PaymentCheckout> { ClosePilotHiddenRoute() }

    composable<Route.PremiumBundles> { ClosePilotHiddenRoute() }

    composable<Route.LocalFinds> { LocalFindsScreen() }

    composable<Route.LocalFindDetail> { entry ->
        val route: Route.LocalFindDetail = entry.toRoute()
        LocalFindDetailScreen(id = route.id, filter = route.filter)
    }
}

/** Pops a destination that pilot visitors must never see (itinerary, payment). */
@Composable
private fun ClosePilotHiddenRoute() {
    val navigator = koinInject<Navigator>()
    LaunchedEffect(Unit) { navigator.navigateUp() }
}
