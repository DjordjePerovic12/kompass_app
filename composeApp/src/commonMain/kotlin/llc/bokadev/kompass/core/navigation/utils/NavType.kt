package llc.bokadev.kompass.core.navigation.utils

import androidx.navigation.NavOptionsBuilder
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.Screen

/**
  Sealed class representing different types of navigation actions.

  Types:
  1. NavigateUp: Simple navigation up/back action
  2. NavigateToRoute: Navigation to a specific screen with optional configuration
     - destination: The target Screen to navigate to
     - navOptions: Optional configuration for how the navigation should behave
  3. SwitchTab: Selects another bottom tab, keeping each tab's own back stack
 **/

sealed class NavType {
    data object NavigateUp : NavType()

    data class NavigateToRoute(
        val destination: Screen,
        val navOptions: NavOptionsBuilder.() -> Unit = {}
    ) : NavType()

    data class SwitchTab(val tab: BottomTab) : NavType()
}
