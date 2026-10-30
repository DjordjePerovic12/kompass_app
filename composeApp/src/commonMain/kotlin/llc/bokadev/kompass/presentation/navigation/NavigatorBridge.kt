package llc.bokadev.kompass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import llc.bokadev.kompass.core.presentation.mvi.NavigationCommand
import llc.bokadev.kompass.core.presentation.mvi.Navigator

/**
 * Collects [navigator]'s command stream and applies each command to [controller] — the only
 * place the new command-bus [Navigator] touches the existing navigation host. [controller]
 * should be whichever per-tab `NavHostController` is currently selected in `MainShell`; pass a
 * new value (e.g. on tab switch) and this effect restarts against it.
 */
@Composable
fun NavigatorBridge(navigator: Navigator, controller: NavHostController, onSwitchTab: (BottomTab) -> Unit = {}) {
    LaunchedEffect(navigator, controller) {
        navigator.commands.collect { command ->
            when (command) {
                is NavigationCommand.NavigateTo -> controller.navigate(command.route) {
                    applyNavOptions(command)
                }
                NavigationCommand.NavigateUp -> controller.navigateUp()
                is NavigationCommand.SwitchTab -> onSwitchTab(command.tab)
            }
        }
    }
}

private fun NavOptionsBuilder.applyNavOptions(command: NavigationCommand.NavigateTo) {
    launchSingleTop = command.isSingleTop
    restoreState = command.restoreState
    command.popUpToRoute?.let { popTarget ->
        popUpTo(popTarget::class) { inclusive = command.popUpToInclusive }
    }
}
