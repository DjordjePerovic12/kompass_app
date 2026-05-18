package llc.bokadev.kompass.presentation.screens.events

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.noRippleClickable
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.presentation.theme.KompassTheme

@Composable
fun EventsScreenContent(
    state: EventsState,
    onDateFilterSelected: (String) -> Unit,
    onTypeFilterSelected: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val colors = KompassTheme.colors
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.colorHomeCanvas),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FilterSection(
                title = "When",
                filters = state.dateFilters,
                selectedKey = state.selectedDateFilter,
                lang = lang,
                onFilterSelected = onDateFilterSelected
            )
        }

        item {
            FilterSection(
                title = "Type",
                filters = state.typeFilters,
                selectedKey = state.selectedTypeFilter,
                lang = lang,
                onFilterSelected = onTypeFilterSelected
            )
        }

        when {
            state.error != null -> item {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorError,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp)
                )
            }

            state.events.isEmpty() && !state.isLoading -> item {
                Text(
                    text = if (state.isShowingFilteredResults()) {
                        strings.noEventsForFilters
                    } else {
                        strings.noEventsYet
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorSlate,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp)
                )
            }

            else -> items(state.events) {
                EventItem(event = it, lang = lang, onClick = { onEventClick(it.id) })
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    filters: List<EventFilter>,
    selectedKey: String,
    lang: String,
    onFilterSelected: (String) -> Unit
) {
    val colors = KompassTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = colors.colorSlate.copy(alpha = 0.78f)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filters) { filter ->
                EventsFilterChip(
                    label = filter.localizedLabel(lang),
                    isSelected = filter.key == selectedKey,
                    onClick = { onFilterSelected(filter.key) }
                )
            }
        }
    }
}

@Composable
private fun EventsFilterChip(
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
                shape = RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .noRippleClickable(onClick)
    )
}

private fun EventsState.isShowingFilteredResults(): Boolean {
    return selectedDateFilter != "all_dates" || selectedTypeFilter != "all_types"
}
