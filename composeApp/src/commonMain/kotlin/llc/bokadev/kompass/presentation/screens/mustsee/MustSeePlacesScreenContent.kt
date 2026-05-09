package llc.bokadev.kompass.presentation.screens.mustsee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.favoritePlacesFirst
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.presentation.screens.category_items_list.components.PlaceListItem
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject

@Composable
fun MustSeePlacesScreenContent(
    state: MustSeePlacesState,
    onIntent: (MustSeePlacesEvent) -> Unit,
    onPlaceClick: (String) -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val favorites by favoritesRepository.favoritesFlow.collectAsState()
    val favoriteKeys = favoritesRepository.getFavoriteKeySet()
    val orderedPlaces = state.places.favoritePlacesFirst(favoriteKeys)

    when {
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Could not load places", color = colors.colorSlate)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Retry",
                        color = colors.colorOrangeMain,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onIntent(MustSeePlacesEvent.LoadPlaces) }
                    )
                }
            }
        }

        state.places.isEmpty() && !state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No must-see places available yet.", color = colors.colorSlate)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.colorWhite),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(orderedPlaces, key = { _, p -> p.id }) { index, place ->
                    PlaceListItem(
                        place = place,
                        lang = lang,
                        isFavorited = favoritesRepository.isFavorited(FavoriteItemType.PLACE, place.id),
                        onFavoriteClick = { favoritesRepository.toggleFavorite(FavoriteItemType.PLACE, place.id) },
                        onClick = { onPlaceClick(place.id) }
                    )
                    if (index < orderedPlaces.lastIndex) {
                        HorizontalDivider(
                            color = colors.colorNavy.copy(alpha = 0.08f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}
