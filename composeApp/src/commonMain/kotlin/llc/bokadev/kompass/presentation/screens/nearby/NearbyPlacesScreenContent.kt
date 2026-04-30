package llc.bokadev.kompass.presentation.screens.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.noRippleClickable
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.presentation.screens.category_items_list.components.PlaceListItem
import llc.bokadev.kompass.presentation.theme.KompassTheme
import kotlin.math.roundToInt

@Composable
fun NearbyPlacesScreenContent(
    state: NearbyPlacesState,
    onIntent: (NearbyPlacesEvent) -> Unit,
    onPlaceClick: (String) -> Unit,
    topBar: @Composable () -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val filteredPlaces = state.nearbyPlaces.filter { nearbyPlace ->
        state.selectedCategory == null || nearbyPlace.place.category == state.selectedCategory
    }
    val walkable = filteredPlaces.filter { it.distanceKm <= 1.5 }
    val shortRide = filteredPlaces.filter { it.distanceKm > 1.5 && it.distanceKm <= 5.0 }
    val byCar = filteredPlaces.filter { it.distanceKm > 5.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorSurface)
    ) {
        topBar()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (!state.isUsingCurrentLocation) {
                    Text(
                        text = "Location unavailable, sorted from Kotor Old Town so you can still explore what is closest first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.colorSlate
                    )
                } else {
                    Text(
                        text = "Walkable spots come first, then short rides and places best reached by car.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.colorSlate
                    )
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        NearbyFilterChip(
                            label = "All",
                            isSelected = state.selectedCategory == null,
                            onClick = { onIntent(NearbyPlacesEvent.SelectCategory(null)) }
                        )
                    }
                    items(PlaceCategory.entries.toList()) { category ->
                        NearbyFilterChip(
                            label = category.uiText,
                            isSelected = state.selectedCategory == category,
                            onClick = { onIntent(NearbyPlacesEvent.SelectCategory(category)) }
                        )
                    }
                }
            }

            when {
                state.isLoading -> item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 24.dp),
                        color = colors.colorAmber
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
                        text = "No places match this filter nearby.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.colorSlate
                    )
                }

                else -> {
                    if (walkable.isNotEmpty()) {
                        item { DistanceSectionTitle("Walkable now") }
                        items(walkable) { nearbyPlace ->
                            NearbyPlaceCard(
                                nearbyPlace = nearbyPlace,
                                lang = lang,
                                onPlaceClick = onPlaceClick
                            )
                        }
                    }

                    if (shortRide.isNotEmpty()) {
                        item { DistanceSectionTitle("Short ride away") }
                        items(shortRide) { nearbyPlace ->
                            NearbyPlaceCard(
                                nearbyPlace = nearbyPlace,
                                lang = lang,
                                onPlaceClick = onPlaceClick
                            )
                        }
                    }

                    if (byCar.isNotEmpty()) {
                        item { DistanceSectionTitle("Worth the drive") }
                        items(byCar) { nearbyPlace ->
                            NearbyPlaceCard(
                                nearbyPlace = nearbyPlace,
                                lang = lang,
                                onPlaceClick = onPlaceClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DistanceSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = KompassTheme.colors.colorNavy
    )
}

@Composable
private fun NearbyFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = KompassTheme.colors
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isSelected) colors.colorWhite else colors.filterUnselectedText,
        modifier = Modifier
            .background(
                color = if (isSelected) colors.colorNavy else colors.filterUnselectedSurface,
                shape = RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .noRippleClickable(onClick)
    )
}

@Composable
private fun NearbyPlaceCard(
    nearbyPlace: NearbyPlace,
    lang: String,
    onPlaceClick: (String) -> Unit
) {
    val place = nearbyPlace.place
    PlaceListItem(
        place = place,
        lang = lang,
        onClick = { onPlaceClick(place.id) }
    )
    Text(
        text = nearbyPlace.distanceKm.toDistanceLabel(),
        style = MaterialTheme.typography.bodySmall,
        color = KompassTheme.colors.colorSlate,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 8.dp)
    )
}

private fun Double.toDistanceLabel(): String {
    return if (this < 1.0) {
        "${(this * 1000).roundToInt()} m away"
    } else {
        "${((this * 10).roundToInt() / 10.0)} km away"
    }
}
