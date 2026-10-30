package llc.bokadev.kompass.core.presentation.mvi

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import llc.bokadev.kompass.presentation.navigation.BottomTab
import llc.bokadev.kompass.presentation.navigation.Route

/**
 * Command-bus navigation: a ViewModel that needs to navigate takes this as a constructor param
 * and calls it directly, instead of a screen threading `onXClick: () -> Unit` callbacks down
 * through `ScreenContent` and every `NavGraphBuilder` function in between. A single
 * `NavigatorBridge` composable (in `presentation/navigation/`) collects [commands] and applies
 * them to the real `NavHostController`.
 *
 * Every screen's ViewModel navigates through this; the shell only owns the tab bar itself.
 */
interface Navigator {
    val commands: Flow<NavigationCommand>

    fun navigateTo(
        route: Route,
        popUpToRoute: Route? = null,
        popUpToInclusive: Boolean = false,
        isSingleTop: Boolean = false,
        restoreState: Boolean = false
    )

    fun navigateUp()

    /** Switches to [tab]'s own back stack (e.g. "Explore Kotor" from an empty Saved). */
    fun switchTab(tab: BottomTab)
}

sealed interface NavigationCommand {
    data class NavigateTo(
        val route: Route,
        val popUpToRoute: Route?,
        val popUpToInclusive: Boolean,
        val isSingleTop: Boolean,
        val restoreState: Boolean
    ) : NavigationCommand

    data object NavigateUp : NavigationCommand

    data class SwitchTab(val tab: BottomTab) : NavigationCommand
}

class NavigatorImpl : Navigator {
    private val _commands = Channel<NavigationCommand>(Channel.UNLIMITED)
    override val commands: Flow<NavigationCommand> = _commands.receiveAsFlow()

    override fun navigateTo(
        route: Route,
        popUpToRoute: Route?,
        popUpToInclusive: Boolean,
        isSingleTop: Boolean,
        restoreState: Boolean
    ) {
        _commands.trySend(
            NavigationCommand.NavigateTo(route, popUpToRoute, popUpToInclusive, isSingleTop, restoreState)
        )
    }

    override fun navigateUp() {
        _commands.trySend(NavigationCommand.NavigateUp)
    }

    override fun switchTab(tab: BottomTab) {
        _commands.trySend(NavigationCommand.SwitchTab(tab))
    }
}
