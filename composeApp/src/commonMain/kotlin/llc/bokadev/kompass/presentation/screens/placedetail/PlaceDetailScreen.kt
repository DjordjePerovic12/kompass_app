package llc.bokadev.kompass.presentation.screens.placedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlaceDetailScreen(
    id: String,
    onBack: () -> Unit = {}
) {
    val vm: PlaceDetailViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(id) { vm.onIntent(PlaceDetailEvent.LoadPlace(id)) }

    PlaceDetailScreenContent(
        state = state,
        onIntent = vm::onIntent,
        onBack = onBack
    )
}
