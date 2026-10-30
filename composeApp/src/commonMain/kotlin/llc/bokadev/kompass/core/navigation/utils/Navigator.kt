package llc.bokadev.kompass.core.navigation.utils

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.flow.Flow
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.Screen

/**
  Interface defining navigation operations for the app.

  ViewModels take it as a constructor parameter and call it from viewModelScope;
  NavigatorBridge applies the emitted NavType events to the selected tab's NavController.

  Properties:
  - navigationFlow: A Flow that emits navigation events (NavType)

  Methods:
  - navigateTo: Navigate to a specific screen with optional navigation options
  - navigateUp: Navigate up (back) in the navigation stack
  - switchTab: Select another bottom tab (e.g. "Explore Kotor" from an empty Saved)
 **/

interface Navigator {
    val navigationFlow: Flow<NavType>

    suspend fun navigateTo(
        destination: Screen,
        navOptions: NavOptionsBuilder.() -> Unit = {}
    )

    suspend fun navigateUp()

    suspend fun switchTab(tab: BottomTab)
}
