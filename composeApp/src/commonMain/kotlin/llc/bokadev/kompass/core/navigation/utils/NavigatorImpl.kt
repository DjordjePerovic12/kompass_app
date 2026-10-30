package llc.bokadev.kompass.core.navigation.utils

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.presentation.mvi.Navigator as LegacyNavigator

/**
  Implementation of the Navigator interface that handles navigation using Kotlin Channels.
  Provided as a Koin single in AppModule.

  Until every screen is migrated, LegacyNavigatorAdapter serves the old non-suspend
  core.presentation.mvi.Navigator from the same channel.
 **/

class NavigatorImpl : Navigator {
    private val _navigationChannel = Channel<NavType>(Channel.UNLIMITED)
    override val navigationFlow = _navigationChannel.receiveAsFlow()

    override suspend fun navigateTo(
        destination: Screen,
        navOptions: NavOptionsBuilder.() -> Unit
    ) {
        _navigationChannel.send(
            NavType.NavigateToRoute(
                destination = destination,
                navOptions = navOptions
            )
        )
    }

    override suspend fun navigateUp() {
        _navigationChannel.send(NavType.NavigateUp)
    }

    override suspend fun switchTab(tab: BottomTab) {
        _navigationChannel.send(NavType.SwitchTab(tab))
    }

    /** Non-suspending send for [LegacyNavigatorAdapter]; the channel is unlimited, so it never drops. */
    internal fun offer(navType: NavType) {
        _navigationChannel.trySend(navType)
    }
}

/**
 * Serves the legacy non-suspend core.presentation.mvi.Navigator from the same NavType stream.
 * Deleted with the legacy interface once every ViewModel uses [Navigator].
 */
class LegacyNavigatorAdapter(private val navigator: NavigatorImpl) : LegacyNavigator {
    override fun navigateTo(
        route: Screen,
        popUpToRoute: Screen?,
        popUpToInclusive: Boolean,
        isSingleTop: Boolean,
        restoreState: Boolean
    ) {
        navigator.offer(
            NavType.NavigateToRoute(route) {
                launchSingleTop = isSingleTop
                this.restoreState = restoreState
                popUpToRoute?.let { popUpTo(it::class) { inclusive = popUpToInclusive } }
            }
        )
    }

    override fun navigateUp() {
        navigator.offer(NavType.NavigateUp)
    }

    override fun switchTab(tab: BottomTab) {
        navigator.offer(NavType.SwitchTab(tab))
    }
}
