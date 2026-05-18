package llc.bokadev.kompass.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.screens.categories.CategoriesScreen
import llc.bokadev.kompass.presentation.screens.experiences.ExperiencesScreen
import llc.bokadev.kompass.presentation.screens.home.HomeScreen
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun MainShell(
    initialTab: BottomTab = BottomTab.Home,
    backTab: BottomTab? = null,
    onSelectRootTab: (BottomTab) -> Unit,
    onSelectRootTabWithBackTarget: (BottomTab, BottomTab?) -> Unit,
    onNavigateToPlaceDetail: (String) -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToExperienceDetail: (String) -> Unit,
    onNavigateToInfoCenterDetail: (String) -> Unit,
    onNavigateToItineraryDetail: (String) -> Unit,
    onNavigateToPlacesList: (String) -> Unit,
    onNavigateToMustSeePlaces: () -> Unit,
    onNavigateToNearbyPlaces: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToInfoCenter: () -> Unit,
    onNavigateToMyGuides: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToChangeLanguage: () -> Unit,
    onNavigateToLocalFinds: () -> Unit
) {
    val selectedTab = initialTab
    var homeVersion by rememberSaveable { mutableIntStateOf(0) }
    var categoriesVersion by rememberSaveable { mutableIntStateOf(0) }
    var activitiesVersion by rememberSaveable { mutableIntStateOf(0) }
    var essentialsVersion by rememberSaveable { mutableIntStateOf(0) }

    fun navigateToTab(tab: BottomTab) {
        if (tab == selectedTab) {
            when (tab) {
                BottomTab.Home -> homeVersion += 1
                BottomTab.Categories -> categoriesVersion += 1
                BottomTab.Activities -> activitiesVersion += 1
                BottomTab.Essentials -> essentialsVersion += 1
            }
            return
        }
        onSelectRootTab(tab)
    }

    fun navigateBackFromRootTab() {
        if (selectedTab == BottomTab.Home) return
        onSelectRootTab(backTab ?: BottomTab.Home)
    }

    Scaffold(
        containerColor = KompassTheme.colors.colorSurface,
        contentWindowInsets = WindowInsets(0)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 82.dp)
            ) {
            when (selectedTab) {
                BottomTab.Home -> HomeScreen(
                    vmKey = "home-$homeVersion",
                    onNavigateToPlaceDetail = onNavigateToPlaceDetail,
                    onNavigateToEventDetail = onNavigateToEventDetail,
                    onNavigateToInfoCenterDetail = onNavigateToInfoCenterDetail,
                    onNavigateToEvents = onNavigateToEvents,
                    onNavigateToItineraryDetail = onNavigateToItineraryDetail,
                    onNavigateToMustSeePlaces = onNavigateToMustSeePlaces,
                    onNavigateToNearbyPlaces = onNavigateToNearbyPlaces,
                    onNavigateToInfoCenter = onNavigateToInfoCenter,
                    onNavigateToMyGuides = onNavigateToMyGuides,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToChangeLanguage = onNavigateToChangeLanguage
                )

                BottomTab.Categories -> CategoriesScreen(
                    vmKey = "categories-$categoriesVersion",
                    onBack = { navigateBackFromRootTab() },
                    onNavigateToPlacesList = onNavigateToPlacesList,
                    onNavigateToNearbyPlaces = onNavigateToNearbyPlaces,
                    onNavigateToActivities = {
                        onSelectRootTabWithBackTarget(BottomTab.Activities, BottomTab.Categories)
                    },
                    onNavigateToEssentials = {
                        onSelectRootTabWithBackTarget(BottomTab.Essentials, BottomTab.Categories)
                    },
                    onNavigateToServices = onNavigateToServices,
                    onNavigateToEvents = onNavigateToEvents,
                    onNavigateToInfoCenter = onNavigateToInfoCenter,
                    onNavigateToFavorites = onNavigateToFavorites,
                    onNavigateToLocalFinds = onNavigateToLocalFinds
                )

                BottomTab.Activities -> ExperiencesScreen(
                    vmKey = "activities-$activitiesVersion",
                    onNavigateToExperienceDetail = onNavigateToExperienceDetail,
                    onBack = { navigateBackFromRootTab() }
                )

                BottomTab.Essentials -> llc.bokadev.kompass.presentation.screens.essentials.EssentialsScreen(
                    vmKey = "essentials-$essentialsVersion",
                    onBack = { navigateBackFromRootTab() }
                )
            }

            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                KompassBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = {
                        navigateToTab(it)
                    }
                )
            }
        }
    }
}
