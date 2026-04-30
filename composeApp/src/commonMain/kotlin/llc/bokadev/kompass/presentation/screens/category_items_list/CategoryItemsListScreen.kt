package llc.bokadev.kompass.presentation.screens.category_items_list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryItemsListScreen(
    category: String,
    onBack: () -> Unit,
    onPlaceClick: (String) -> Unit
) {
    val vm: CategoryItemsListViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(category) { vm.init(category) }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = state.selectedCategory.slug,
                title = state.selectedCategory.displayLabel,
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        CategoryItemsListScreenContent(
            state = state,
            onIntent = vm::onIntent,
            onPlaceClick = onPlaceClick,
            onBack = onBack
        )
    }
}

private val PlaceCategory.displayLabel: String
    get() = when (this) {
        PlaceCategory.EAT_AND_DRINK -> "Eat & Drink"
        PlaceCategory.SEE_AND_VISIT -> "See & Visit"
        PlaceCategory.ACTIVITIES    -> "Activities"
        PlaceCategory.HIDDEN_GEMS   -> "Hidden Gems"
        PlaceCategory.PRACTICAL     -> "Practical"
    }

private val PlaceCategory.slug: String
    get() = when (this) {
        PlaceCategory.EAT_AND_DRINK -> "Restaurants, bars & cafés"
        PlaceCategory.SEE_AND_VISIT -> "Landmarks & heritage sites"
        PlaceCategory.ACTIVITIES    -> "Things to do in Kotor"
        PlaceCategory.HIDDEN_GEMS   -> "Local secrets & off-beat spots"
        PlaceCategory.PRACTICAL     -> "Transport, tips & essentials"
    }
