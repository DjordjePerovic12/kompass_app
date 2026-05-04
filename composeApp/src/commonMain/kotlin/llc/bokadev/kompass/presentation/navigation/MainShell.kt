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
import llc.bokadev.kompass.presentation.screens.events.EventsScreen
import llc.bokadev.kompass.presentation.screens.experiences.ExperiencesScreen
import llc.bokadev.kompass.presentation.screens.home.HomeScreen
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun MainShell(
    onNavigateToPlaceDetail: (String) -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToExperienceDetail: (String) -> Unit,
    onNavigateToItineraryDetail: (String) -> Unit,
    onNavigateToPlacesList: (String) -> Unit,
    onNavigateToActivities: () -> Unit,
    onNavigateToNearbyPlaces: () -> Unit,
    onNavigateToEssentials: () -> Unit,
    onNavigateToServices: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }
    var homeVersion by remember { mutableIntStateOf(0) }
    var categoriesVersion by remember { mutableIntStateOf(0) }
    var eventsVersion by remember { mutableIntStateOf(0) }
    var experiencesVersion by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = KompassTheme.colors.colorSurface,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            KompassBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    when (it) {
                        BottomTab.Home        -> homeVersion += 1
                        BottomTab.Categories  -> categoriesVersion += 1
                        BottomTab.Events      -> eventsVersion += 1
                        BottomTab.Experiences -> experiencesVersion += 1
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
                    onNavigateToItineraryDetail = onNavigateToItineraryDetail,
                    onNavigateToNearbyPlaces = onNavigateToNearbyPlaces
                )
                BottomTab.Categories -> CategoriesScreen(
                    vmKey = "categories-$categoriesVersion",
                    onNavigateToPlacesList = onNavigateToPlacesList,
                    onNavigateToActivities = onNavigateToActivities,
                    onNavigateToEssentials = onNavigateToEssentials,
                    onNavigateToServices = onNavigateToServices
                )
                BottomTab.Events -> EventsScreen(
                    vmKey = "events-$eventsVersion",
                    onNavigateToEventDetail = onNavigateToEventDetail
                )
                BottomTab.Experiences -> ExperiencesScreen(
                    vmKey = "experiences-$experiencesVersion",
                    onNavigateToExperienceDetail = onNavigateToExperienceDetail
                )
            }
        }
    }
}
