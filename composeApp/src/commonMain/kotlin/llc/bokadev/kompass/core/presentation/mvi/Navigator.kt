package llc.bokadev.kompass.core.presentation.mvi

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.Screen

/**
 * Legacy non-suspend navigation API, kept only until every ViewModel moves to
 * core.navigation.utils.Navigator in Phase 3. The production instance is
 * core.navigation.utils.NavigatorImpl, which implements both.
 */
interface Navigator {
    fun navigateTo(
        route: Screen,
        popUpToRoute: Screen? = null,
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
        val route: Screen,
        val popUpToRoute: Screen?,
        val popUpToInclusive: Boolean,
        val isSingleTop: Boolean,
        val restoreState: Boolean
    ) : NavigationCommand

    data object NavigateUp : NavigationCommand

    data class SwitchTab(val tab: BottomTab) : NavigationCommand
}
