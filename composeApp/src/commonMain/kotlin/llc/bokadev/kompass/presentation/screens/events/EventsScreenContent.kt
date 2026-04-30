package llc.bokadev.kompass.presentation.screens.events
import llc.bokadev.kompass.core.util.currentAppLanguage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.shared.FilterChip
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun EventsScreenContent(
    state: EventsState,
    onDateFilterSelected: (String) -> Unit,
    onTypeFilterSelected: (String) -> Unit,
    onEventClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    alignment = Alignment.Start
                ),
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp)
            ) {
                items(state.dateFilters) {
                    FilterChip(
                        filter = it,
                        lang = lang,
                        isSelected = it.key == state.selectedDateFilter,
                        onFilterSelected = {
                            onDateFilterSelected(it)
                        }
                    )
                }
            }
        }

        item {
            HorizontalDivider(
                color = KompassTheme.colors.colorNavy,
                modifier = Modifier.height(1.dp)
            )
        }

        item {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    alignment = Alignment.Start
                ),
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp)
            ) {
                items(state.typeFilters) {
                    FilterChip(
                        filter = it,
                        lang = lang,
                        isSelected = it.key == state.selectedTypeFilter,
                        onFilterSelected = {
                            onTypeFilterSelected(it)
                        }
                    )
                }
            }
        }

        when {
            state.error != null -> item {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorError,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            state.events.isEmpty() && !state.isLoading -> item {
                Text(
                    text = if (state.isShowingFilteredResults()) {
                        "No events match these filters"
                    } else {
                        "No events added yet"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            else -> items(state.events) {
                EventItem(event = it, lang = lang)
            }
        }
    }

}

private fun EventsState.isShowingFilteredResults(): Boolean {
    return selectedDateFilter != "all_dates" || selectedTypeFilter != "all_types"
}
