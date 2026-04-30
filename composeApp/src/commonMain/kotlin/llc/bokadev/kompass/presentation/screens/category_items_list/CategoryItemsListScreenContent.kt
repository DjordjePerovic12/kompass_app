package llc.bokadev.kompass.presentation.screens.category_items_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.screens.category_items_list.components.PlaceListItem
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun CategoryItemsListScreenContent(
    state: CategoryItemsListState,
    onIntent: (CategoryItemsListEvent) -> Unit,
    onPlaceClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val colors = KompassTheme.colors

    when {
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Could not load places", color = colors.colorSlate)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Retry",
                        color = colors.colorAmberDark,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onIntent(CategoryItemsListEvent.LoadPlaces) }
                    )
                }
            }
        }

        state.places.isEmpty() && !state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nothing here yet", color = colors.colorSlate)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.colorWhite),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(state.places, key = { _, p -> p.id }) { index, place ->
                    PlaceListItem(
                        place = place,
                        onClick = { onPlaceClick(place.id) }
                    )
                    if (index < state.places.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier,
                            color = colors.colorSlateGhost,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}
