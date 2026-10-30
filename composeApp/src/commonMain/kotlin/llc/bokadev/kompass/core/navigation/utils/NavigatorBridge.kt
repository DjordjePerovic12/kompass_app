package llc.bokadev.kompass.core.navigation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import llc.bokadev.kompass.core.components.BottomTab

/**
 * Applies [navigator]'s events to [controller], the NavController of the selected tab in
 * MainShell (or the root controller during first launch). Passing a new controller on tab
 * switch restarts collection against it.
 */
@Composable
fun NavigatorBridge(
    navigator: Navigator,
    controller: NavHostController,
    onSwitchTab: (BottomTab) -> Unit = {}
) {
    ObserveAsEvents(flow = navigator.navigationFlow, key1 = controller) { navType ->
        when (navType) {
            is NavType.NavigateToRoute -> controller.navigate(navType.destination) {
                navType.navOptions(this)
            }

            NavType.NavigateUp -> controller.navigateUp()
            is NavType.SwitchTab -> onSwitchTab(navType.tab)
        }
    }
}
