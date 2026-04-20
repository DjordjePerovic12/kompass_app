package llc.bokadev.kompass.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import llc.bokadev.kompass.presentation.screens.categories.CategoriesScreen
import llc.bokadev.kompass.presentation.screens.essentials.EssentialsScreen
import llc.bokadev.kompass.presentation.screens.events.EventsScreen
import llc.bokadev.kompass.presentation.screens.experiences.ExperiencesScreen
import llc.bokadev.kompass.presentation.screens.home.HomeScreen
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun MainShell(
    onNavigateToPlaceDetail: (String) -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToExperienceDetail: (String) -> Unit,
    onNavigateToItineraryDetail: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }

    Scaffold(
        containerColor = KompassTheme.colors.colorSurface,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            KompassBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            when (selectedTab) {
                BottomTab.Home -> HomeScreen(
                    onNavigateToPlaceDetail = onNavigateToPlaceDetail,
                    onNavigateToEventDetail = onNavigateToEventDetail,
                    onNavigateToItineraryDetail = onNavigateToItineraryDetail
                )
                BottomTab.Categories -> CategoriesScreen(
                    onNavigateToPlaceDetail = onNavigateToPlaceDetail
                )
                BottomTab.Events -> EventsScreen(
                    onNavigateToEventDetail = onNavigateToEventDetail
                )
                BottomTab.Experiences -> ExperiencesScreen(
                    onNavigateToExperienceDetail = onNavigateToExperienceDetail
                )
                BottomTab.Essentials -> EssentialsScreen()
            }
        }
    }
}
