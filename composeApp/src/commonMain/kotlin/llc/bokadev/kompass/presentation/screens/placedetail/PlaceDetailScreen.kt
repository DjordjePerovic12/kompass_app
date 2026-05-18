package llc.bokadev.kompass.presentation.screens.placedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlaceDetailScreen(
    id: String,
    onBack: () -> Unit = {},
    onLearnMore: () -> Unit = {}
) {
    val vm: PlaceDetailViewModel = koinViewModel()
    val freeGuideVm: PlaceGuideViewModel = koinViewModel(
        key = "place-guide-free-$id",
        parameters = { parametersOf(id, false, false) }
    )
    val deepGuideVm: PlaceGuideViewModel = koinViewModel(
        key = "place-guide-deep-$id",
        parameters = { parametersOf(id, false, true) }
    )
    val state by vm.state.collectAsState()
    val freeGuideState by freeGuideVm.state.collectAsState()
    val deepGuideState by deepGuideVm.state.collectAsState()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val favorites by favoritesRepository.favoritesFlow.collectAsState()

    LaunchedEffect(id) { vm.onIntent(PlaceDetailEvent.LoadPlace(id)) }

    PlaceDetailScreenContent(
        state = state,
        isFavorited = favorites.any { it.type == FavoriteItemType.PLACE && it.id == id },
        onFavoriteClick = { favoritesRepository.toggleFavorite(FavoriteItemType.PLACE, id) },
        onIntent = vm::onIntent,
        onBack = onBack,
        onLearnMore = onLearnMore,
        freeGuideState = freeGuideState,
        onFreeGuideIntent = freeGuideVm::onIntent,
        deepGuideState = deepGuideState,
        onDeepGuideIntent = deepGuideVm::onIntent
    )
}
