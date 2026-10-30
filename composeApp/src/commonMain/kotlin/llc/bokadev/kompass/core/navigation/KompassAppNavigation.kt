package llc.bokadev.kompass.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import llc.bokadev.kompass.core.navigation.destinations.categoryListScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.essentialModuleScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.essentialsScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.eventDetailScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.eventsScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.experienceDetailScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.exploreScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.favoritesScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.homeScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.infoCenterDetailScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.infoCenterScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.languagePickerScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.listenScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.localFindDetailScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.localFindsScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.mainScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.placeDetailScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.searchScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.serviceDetailScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.servicesScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.utilityMapScreenComposable
import llc.bokadev.kompass.core.navigation.destinations.worthTheDriveScreenComposable

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

/**
 * Root navigation: first-launch language picker, then the tab shell. Each tab runs its own
 * NavHost inside MainShell with [tabDestinations]; ViewModels navigate through the injected
 * Navigator and NavigatorBridge applies it to the selected tab's controller.
 */
@Composable
fun KompassAppNavigation(isFirstLaunch: Boolean) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (isFirstLaunch) Screen.LanguagePickerScreen else Screen.MainScreen,
        enterTransition = kompassEnterTransition,
        exitTransition = kompassExitTransition,
        popEnterTransition = kompassPopEnterTransition,
        popExitTransition = kompassPopExitTransition
    ) {
        languagePickerScreenComposable(navController = navController)
        mainScreenComposable()
    }
}

/** Every destination reachable inside a tab's graph. */
internal fun NavGraphBuilder.tabDestinations() {
    homeScreenComposable()
    exploreScreenComposable()
    favoritesScreenComposable()
    essentialsScreenComposable()
    essentialModuleScreenComposable()
    utilityMapScreenComposable()
    eventsScreenComposable()
    eventDetailScreenComposable()
    placeDetailScreenComposable()
    experienceDetailScreenComposable()
    categoryListScreenComposable()
    worthTheDriveScreenComposable()
    servicesScreenComposable()
    serviceDetailScreenComposable()
    infoCenterScreenComposable()
    infoCenterDetailScreenComposable()
    localFindsScreenComposable()
    localFindDetailScreenComposable()
    listenScreenComposable()
    searchScreenComposable()
}
