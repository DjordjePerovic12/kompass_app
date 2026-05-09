package llc.bokadev.kompass.presentation.screens.mustsee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.core.presentation.base.BaseContentView
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import llc.bokadev.kompass.presentation.shared.KompassSharedTopBar
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MustSeePlacesScreen(
    onBack: () -> Unit,
    onPlaceClick: (String) -> Unit
) {
    val vm: MustSeePlacesViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val analytics = koinInject<AnalyticsRepository>()

    LaunchedEffect(Unit) {
        vm.init()
        analytics.trackScreenView("must_see_places")
    }

    BaseContentView(
        state = state,
        topBar = {
            KompassSharedTopBar(
                slug = "Curated landmarks and first stops",
                title = "Must See",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) {
        MustSeePlacesScreenContent(
            state = state,
            onIntent = vm::onIntent,
            onPlaceClick = { id ->
                state.places.firstOrNull { it.id == id }?.let { place ->
                    analytics.trackPlaceView(
                        placeId = place.id,
                        cityId = place.cityId,
                        zone = place.zone,
                        placeCategory = place.category.name.lowercase(),
                        contentOrigin = "must_see_screen"
                    )
                }
                onPlaceClick(id)
            }
        )
    }
}
