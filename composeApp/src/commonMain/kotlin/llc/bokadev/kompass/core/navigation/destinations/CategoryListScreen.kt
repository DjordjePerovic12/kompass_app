package llc.bokadev.kompass.core.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import llc.bokadev.kompass.core.navigation.Screen
import llc.bokadev.kompass.presentation.screens.categorylist.CategoryListScreen

fun NavGraphBuilder.categoryListScreenComposable() {
    composable<Screen.CategoryListScreen> { entry ->
        val route = entry.toRoute<Screen.CategoryListScreen>()
        CategoryListScreen(kind = route.kind)
    }
}
