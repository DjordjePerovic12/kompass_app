package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.MainShell
import llc.bokadev.kompass.core.navigation.Screen

fun NavGraphBuilder.mainScreenComposable() {
    composable<Screen.MainScreen> {
        MainShell()
    }
}
