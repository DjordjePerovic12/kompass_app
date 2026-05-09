package llc.bokadev.kompass.presentation.screens.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesScreen(
    vmKey: String = "categories",
    onBack: () -> Unit = {},
    onNavigateToPlacesList: (String) -> Unit = {},
    onNavigateToNearbyPlaces: () -> Unit = {},
    onNavigateToActivities: () -> Unit = {},
    onNavigateToEssentials: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToInfoCenter: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {}
) {
    val vm: CategoriesViewModel = koinViewModel(key = vmKey)
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()
    val strings = rememberAppStrings()

    LaunchedEffect(Unit) {
        analytics.trackScreenView("browse")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = strings.heroLocation,
                title = strings.browseTitle,
                subtitle = "Curated entry points into the city",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        CategoriesScreenContent(
            state = state,
            onIntent = vm::onIntent,
            onCategoryClick = { category ->
                analytics.trackCategoryView(category.id, contentOrigin = "browse")
                when (category.id) {
                    "practical" -> onNavigateToEssentials()
                    "activities" -> onNavigateToActivities()
                    else -> onNavigateToPlacesList(category.id.uppercase())
                }
            },
            onPlacesClick = onNavigateToNearbyPlaces,
            onFavoritesClick = onNavigateToFavorites,
            strings = strings,
            onServicesClick = onNavigateToServices,
            onEventsClick = onNavigateToEvents,
            onInfoCenterClick = onNavigateToInfoCenter
        )
    }
}
