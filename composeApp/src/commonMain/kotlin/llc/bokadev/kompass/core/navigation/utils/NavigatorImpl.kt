package llc.bokadev.kompass.core.navigation.utils

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import llc.bokadev.kompass.core.components.BottomTab
import llc.bokadev.kompass.core.navigation.Screen

/**
  Implementation of the Navigator interface that handles navigation using Kotlin Channels.
  Provided as a Koin single in AppModule.
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
}
