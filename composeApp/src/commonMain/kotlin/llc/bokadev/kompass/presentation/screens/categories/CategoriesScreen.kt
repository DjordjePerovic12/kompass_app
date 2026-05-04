package llc.bokadev.kompass.presentation.screens.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesScreen(
    vmKey: String = "categories",
    onNavigateToPlacesList: (String) -> Unit = {},
    onNavigateToActivities: () -> Unit = {},
    onNavigateToEssentials: () -> Unit = {},
    onNavigateToServices: () -> Unit = {}
) {
    val vm: CategoriesViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "What are you looking for",
                title = "Browse",
                onBackClick = {}
            )
        }
    ) {
        CategoriesScreenContent(
            state = state,
            onIntent = vm::onIntent,
            onCategoryClick = { category ->
                if (category.id == "practical") {
                    onNavigateToEssentials()
                } else if (category.id == "activities") {
                    onNavigateToActivities()
                } else {
                    onNavigateToPlacesList(category.id.uppercase())
                }
            },
            onServicesClick = onNavigateToServices
        )
    }
}
