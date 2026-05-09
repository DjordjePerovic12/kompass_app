package llc.bokadev.kompass.presentation.screens.placedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlaceDetailScreen(
    id: String,
    onBack: () -> Unit = {},
    onLearnMore: () -> Unit = {},
    onOpenGuide: (String) -> Unit = {}
) {
    val vm: PlaceDetailViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val favorites by favoritesRepository.favoritesFlow.collectAsState()

    LaunchedEffect(id) { vm.onIntent(PlaceDetailEvent.LoadPlace(id)) }

    PlaceDetailScreenContent(
        state = state,
        isFavorited = favoritesRepository.isFavorited(FavoriteItemType.PLACE, id),
        onFavoriteClick = { favoritesRepository.toggleFavorite(FavoriteItemType.PLACE, id) },
        onIntent = vm::onIntent,
        onBack = onBack,
        onLearnMore = onLearnMore,
        onOpenGuide = onOpenGuide
    )
}
