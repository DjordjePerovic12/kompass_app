package llc.bokadev.kompass.presentation.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToPlaceDetail: (String) -> Unit = {},
    onNavigateToEventDetail: (String) -> Unit = {},
    onNavigateToItineraryDetail: (String) -> Unit = {}
) {
    val vm: HomeViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    HomeScreenContent(
        state = state,
        onPlaceClick = onNavigateToPlaceDetail,
        onEventClick = onNavigateToEventDetail,
        onIntent = vm::onEvent
    )
}
