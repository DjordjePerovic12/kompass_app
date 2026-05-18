package llc.bokadev.kompass.presentation.screens.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.noRippleClickable
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.model.favoriteNearbyFirst
import llc.bokadev.kompass.domain.model.isMarkedMustSee
import llc.bokadev.kompass.domain.repository.FavoritesRepository
import llc.bokadev.kompass.presentation.screens.category_items_list.components.PlaceListItem
import llc.bokadev.kompass.presentation.theme.KompassTheme
import org.koin.compose.koinInject
import kotlin.math.roundToInt

private enum class PlacesFilter(val label: String) {
    ALL("Places"),
    MUST_SEE("Must See"),
    RESTAURANTS("Restaurants"),
    NEARBY("Nearby"),
    WORTH_THE_DRIVE("Worth the drive")
}

@Composable
fun NearbyPlacesScreenContent(
    state: NearbyPlacesState,
    onIntent: (NearbyPlacesEvent) -> Unit,
    onPlaceClick: (String) -> Unit,
    topBar: @Composable () -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val favoritesRepository = koinInject<FavoritesRepository>()
    val favorites by favoritesRepository.favoritesFlow.collectAsState()
    val favoriteKeys = favorites.map { FavoriteKey(it.type, it.id) }.toSet()
    var selectedFilter by remember { mutableStateOf(PlacesFilter.ALL) }

    val filteredPlaces = remember(state.nearbyPlaces, selectedFilter, favorites) {
        state.nearbyPlaces.filter { nearbyPlace ->
            when (selectedFilter) {
                PlacesFilter.ALL -> true
                PlacesFilter.MUST_SEE -> nearbyPlace.place.isMarkedMustSee()
                PlacesFilter.RESTAURANTS -> nearbyPlace.place.category == PlaceCategory.EAT_AND_DRINK
                PlacesFilter.NEARBY -> nearbyPlace.distanceKm <= 1.8
                PlacesFilter.WORTH_THE_DRIVE -> nearbyPlace.distanceKm > 3.5
            }
        }
    }.favoriteNearbyFirst(favoriteKeys)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorHomeCanvas)
    ) {
        topBar()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(PlacesFilter.entries.toList()) { filter ->
                        PlacesFilterChip(
                            label = filter.label,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }

            when {
                state.isLoading -> item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 24.dp),
                        color = colors.colorOrangeMain
                    )
                }

                state.error != null -> item {
                    Text(
                        text = state.error ?: "Something went wrong",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.colorError
                    )
                }

                filteredPlaces.isEmpty() -> item {
                    Text(
                        text = when (selectedFilter) {
                            PlacesFilter.ALL -> "No places available right now."
                            PlacesFilter.MUST_SEE -> "No must-see places available right now."
                            PlacesFilter.RESTAURANTS -> "No restaurants match this view right now."
                            PlacesFilter.NEARBY -> if (state.isUsingCurrentLocation) {
                                "Nothing especially close right now."
                            } else {
                                "Nothing especially close around Old Town right now."
                            }
                            PlacesFilter.WORTH_THE_DRIVE -> "No farther places worth the drive right now."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.colorSlate
                    )
                }

                else -> {
                    items(filteredPlaces, key = { it.place.id }) { nearbyPlace ->
                        NearbyPlacesListCard(
                            nearbyPlace = nearbyPlace,
                            lang = lang,
                            isFavorited = FavoriteKey(FavoriteItemType.PLACE, nearbyPlace.place.id) in favoriteKeys,
                            onFavoriteClick = { favoritesRepository.toggleFavorite(FavoriteItemType.PLACE, nearbyPlace.place.id) },
                            onPlaceClick = onPlaceClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlacesFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isSelected) colors.colorWhite else colors.filterUnselectedText.copy(alpha = 0.82f),
        modifier = Modifier
            .background(
                color = if (isSelected) colors.colorOrangeMain else colors.filterUnselectedSurface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .noRippleClickable(onClick)
    )
}

@Composable
private fun NearbyPlacesListCard(
    nearbyPlace: NearbyPlace,
    lang: String,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    onPlaceClick: (String) -> Unit
) {
    val place = nearbyPlace.place
    val topMeta = listOfNotNull(
        place.category.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() },
        place.zone?.replace('_', ' ')?.replace('-', ' ')?.takeIf { it.isNotBlank() }
    ).joinToString(" · ")
    val bottomMeta = listOfNotNull(
        nearbyPlace.distanceKm.toDistanceLabel(),
        place.subCategory?.replace('_', ' ')?.replace('-', ' ')?.takeIf { it.isNotBlank() },
        place.bestTime.toNearbyLabel()
    ).joinToString(" · ")

    PlaceListItem(
        place = place,
        lang = lang,
        topMetaOverride = topMeta,
        bottomMetaOverride = bottomMeta,
        isFavorited = isFavorited,
        onFavoriteClick = onFavoriteClick,
        onClick = { onPlaceClick(place.id) }
    )
}

private fun Double.toDistanceLabel(): String {
    return if (this < 1.0) {
        "${(this * 1000).roundToInt()} m away"
    } else {
        "${((this * 10).roundToInt() / 10.0)} km away"
    }
}

private fun llc.bokadev.kompass.domain.model.BestTime.toNearbyLabel(): String = when (this) {
    llc.bokadev.kompass.domain.model.BestTime.MORNING -> "Best in morning"
    llc.bokadev.kompass.domain.model.BestTime.AFTERNOON -> "Best in afternoon"
    llc.bokadev.kompass.domain.model.BestTime.EVENING -> "Best in evening"
    llc.bokadev.kompass.domain.model.BestTime.ANYTIME -> "Any time"
}
