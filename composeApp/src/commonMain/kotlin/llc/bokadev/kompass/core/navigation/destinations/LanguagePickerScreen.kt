package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.navigation.utils.NavigatorBridge
import llc.bokadev.kompass.presentation.screens.languagepicker.LanguagePickerScreen
import org.koin.compose.koinInject

fun NavGraphBuilder.languagePickerScreenComposable(navController: NavHostController) {
    composable<Screen.LanguagePickerScreen> {
        // Only first launch lives on the root controller; MainShell's bridge serves the tabs.
        NavigatorBridge(koinInject<Navigator>(), navController)
        LanguagePickerScreen()
    }
}
