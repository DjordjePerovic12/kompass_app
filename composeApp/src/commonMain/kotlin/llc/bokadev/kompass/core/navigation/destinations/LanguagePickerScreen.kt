package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.core.navigation.utils.Navigator
import llc.bokadev.kompass.core.navigation.utils.NavigatorBridge
import llc.bokadev.kompass.core.presentation.SnackbarBus
import llc.bokadev.kompass.presentation.languagepicker.LanguagePickerScreen
import llc.bokadev.kompass.presentation.languagepicker.LanguagePickerViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.languagePickerScreenComposable(navController: NavHostController) {
    composable<Screen.LanguagePickerScreen> {
        // Only first launch lives on the root controller; MainShell's bridge serves the tabs.
        NavigatorBridge(koinInject<Navigator>(), navController)
        val viewModel = koinViewModel<LanguagePickerViewModel>()
        val snackbarBus = koinInject<SnackbarBus>()
        LanguagePickerScreen(
            viewModel = viewModel,
            showSnackBar = snackbarBus::show
        )
    }
}
