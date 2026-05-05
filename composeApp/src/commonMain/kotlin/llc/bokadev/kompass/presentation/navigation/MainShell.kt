package llc.bokadev.kompass.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import llc.bokadev.kompass.presentation.screens.categories.CategoriesScreen
import llc.bokadev.kompass.presentation.screens.experiences.ExperiencesScreen
import llc.bokadev.kompass.presentation.screens.home.HomeScreen
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun MainShell(
    onNavigateToPlaceDetail: (String) -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToExperienceDetail: (String) -> Unit,
    onNavigateToInfoCenterDetail: (String) -> Unit,
    onNavigateToItineraryDetail: (String) -> Unit,
    onNavigateToPlacesList: (String) -> Unit,
    onNavigateToActivities: () -> Unit,
    onNavigateToNearbyPlaces: () -> Unit,
    onNavigateToEssentials: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToInfoCenter: () -> Unit,
    onNavigateToMyGuides: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }
    var homeVersion by remember { mutableIntStateOf(0) }
    var categoriesVersion by remember { mutableIntStateOf(0) }
    var activitiesVersion by remember { mutableIntStateOf(0) }
    var essentialsVersion by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = KompassTheme.colors.colorSurface,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            KompassBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    when (it) {
                        BottomTab.Home -> homeVersion += 1
                        BottomTab.Categories -> categoriesVersion += 1
                        BottomTab.Activities -> activitiesVersion += 1
                        BottomTab.Essentials -> essentialsVersion += 1
                    }
                    selectedTab = it
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            when (selectedTab) {
                BottomTab.Home -> HomeScreen(
                    vmKey = "home-$homeVersion",
                    onNavigateToPlaceDetail = onNavigateToPlaceDetail,
                    onNavigateToEventDetail = onNavigateToEventDetail,
                    onNavigateToInfoCenterDetail = onNavigateToInfoCenterDetail,
                    onNavigateToEvents = onNavigateToEvents,
                    onNavigateToItineraryDetail = onNavigateToItineraryDetail,
                    onNavigateToNearbyPlaces = onNavigateToNearbyPlaces,
                    onNavigateToInfoCenter = onNavigateToInfoCenter,
                    onNavigateToMyGuides = onNavigateToMyGuides
                )

                BottomTab.Categories -> CategoriesScreen(
                    vmKey = "categories-$categoriesVersion",
                    onNavigateToPlacesList = onNavigateToPlacesList,
                    onNavigateToActivities = onNavigateToActivities,
                    onNavigateToEssentials = onNavigateToEssentials,
                    onNavigateToServices = onNavigateToServices,
                    onNavigateToEvents = onNavigateToEvents,
                    onNavigateToInfoCenter = onNavigateToInfoCenter
                )

                BottomTab.Activities -> ExperiencesScreen(
                    vmKey = "activities-$activitiesVersion",
                    onNavigateToExperienceDetail = onNavigateToExperienceDetail
                )

                BottomTab.Essentials -> llc.bokadev.kompass.presentation.screens.essentials.EssentialsScreen(
                    vmKey = "essentials-$essentialsVersion",
                    showBack = false
                )
            }
        }
    }
}
