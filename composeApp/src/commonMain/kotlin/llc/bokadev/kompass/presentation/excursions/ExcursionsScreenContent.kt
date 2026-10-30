package llc.bokadev.kompass.presentation.excursions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.excursions_back
import kompass.composeapp.generated.resources.excursions_cached
import kompass.composeapp.generated.resources.excursions_empty
import kompass.composeapp.generated.resources.excursions_interests
import kompass.composeapp.generated.resources.excursions_intro
import kompass.composeapp.generated.resources.excursions_no_matches
import kompass.composeapp.generated.resources.excursions_planning
import kompass.composeapp.generated.resources.excursions_reset
import kompass.composeapp.generated.resources.excursions_time
import kompass.composeapp.generated.resources.redesign_worth_drive
import kompass.composeapp.generated.resources.retry
import llc.bokadev.kompass.domain.model.Excursion
import llc.bokadev.kompass.domain.model.ExcursionFilter
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.presentation.excursions.components.ExcursionRow
import llc.bokadev.kompass.presentation.excursions.components.excursionLabelRes
import llc.bokadev.kompass.ui.theme.KompassTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Worth the Drive: an editorial collection with interest/time/planning filters.
 * Loading and failure render inline below the header and filters, so the back button stays put.
 */
@Composable
fun ExcursionsScreenContent(
    modifier: Modifier = Modifier,
    state: ExcursionsState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onFilterChange: (ExcursionFilter) -> Unit,
    onSaveClick: (type: FavoriteItemType, id: String, saved: Boolean) -> Unit,
    onExcursionClick: (type: FavoriteItemType, id: String) -> Unit
) {
    val colors = KompassTheme.colors
    val filter = state.filter
    val visible = state.items.filter(filter::matches)
    val retryLabel = stringResource(Res.string.retry)
    Column(modifier.fillMaxSize().background(colors.nightBlack).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            TextButton(onClick = onBackClick) { Text(stringResource(Res.string.excursions_back), color = colors.lampYellow) }
        }
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item {
                Text(stringResource(Res.string.redesign_worth_drive), style = KompassTheme.typography.gloockRegular40, color = colors.boneWhite)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(Res.string.excursions_intro), style = KompassTheme.typography.geologicaMedium16, color = colors.stoneGray)
            }
            if (state.items.isNotEmpty()) {
                item {
                    FilterGroup(stringResource(Res.string.excursions_interests), listOf("culture", "nature", "hiking", "water").filter { key -> state.items.any { key in it.interests } }, filter.interests) { key ->
                        onFilterChange(filter.copy(interests = if (key in filter.interests) filter.interests - key else filter.interests + key))
                    }
                    FilterGroup(stringResource(Res.string.excursions_time), listOf("short", "half_day", "full_day").filter { key -> state.items.any { it.timeBand == key } }, setOfNotNull(filter.timeBand)) { key ->
                        onFilterChange(filter.copy(timeBand = key.takeUnless { it == filter.timeBand }))
                    }
                    FilterGroup(stringResource(Res.string.excursions_planning), listOf("independent", "arrange_ahead").filter { key -> state.items.any { it.planningLevel == key } }, setOfNotNull(filter.planningLevel)) { key ->
                        onFilterChange(filter.copy(planningLevel = key.takeUnless { it == filter.planningLevel }))
                    }
                }
            }
            if (filter != ExcursionFilter()) item {
                TextButton(onClick = { onFilterChange(ExcursionFilter()) }) { Text(stringResource(Res.string.excursions_reset), color = colors.lampYellow) }
            }
            if (state.cached) item {
                Text(stringResource(Res.string.excursions_cached), color = colors.stoneGray, style = KompassTheme.typography.geologicaMedium16)
                TextButton(onClick = onRetryClick) { Text(retryLabel, color = colors.lampYellow) }
            }
            val failure = state.error
            when {
                state.isLoading -> item { CircularProgressIndicator(color = colors.lampYellow) }
                failure != null -> item {
                    Text(stringResource(failure.messageRes), color = colors.boneWhite)
                    TextButton(onClick = onRetryClick) { Text(retryLabel, color = colors.lampYellow) }
                }
                visible.isEmpty() -> item {
                    Text(stringResource(if (state.items.isEmpty()) Res.string.excursions_empty else Res.string.excursions_no_matches), color = colors.stoneGray)
                }
            }
            items(visible, key = { it.id }) { entry ->
                val type = if (entry.place != null) FavoriteItemType.PLACE else FavoriteItemType.ACTIVITY
                val id = entry.place?.id ?: entry.activity!!.id
                val saved = FavoriteKey(type, id) in state.savedKeys
                ExcursionRow(entry, saved, { onSaveClick(type, id, !saved) }) {
                    onExcursionClick(type, id)
                }
            }
        }
    }
}

@Composable
private fun FilterGroup(title: String, values: List<String>, selected: Set<String>, onClick: (String) -> Unit) {
    if (values.isEmpty()) return
    val colors = KompassTheme.colors
    Text(title, style = KompassTheme.typography.geologicaMedium16, color = colors.stoneGray)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(values) { value -> FilterChip(
            selected = value in selected, onClick = { onClick(value) },
            label = { Text(excursionLabelRes(value)?.let { stringResource(it) }.orEmpty()) },
            colors = FilterChipDefaults.filterChipColors(containerColor = colors.charcoal, labelColor = colors.boneWhite,
                selectedContainerColor = colors.lampYellow, selectedLabelColor = colors.nightBlack),
            modifier = Modifier.heightIn(min = 48.dp)
        ) }
    }
}

@Preview
@Composable
private fun ExcursionsScreenContentPreview() {
    KompassTheme {
        ExcursionsScreenContent(
            state = ExcursionsState(
                isLoading = false,
                items = listOf(
                    Excursion(
                        id = "islands",
                        activity = Experience(id = "islands", cityId = "kotor", name = mapOf("en" to "Perast islands"), description = emptyMap(), photos = emptyList()),
                        interests = setOf("culture", "water"),
                        timeBand = "half_day",
                        planningLevel = "arrange_ahead"
                    )
                ),
                filter = ExcursionFilter(interests = setOf("water"))
            ),
            onBackClick = {},
            onRetryClick = {},
            onFilterChange = {},
            onSaveClick = { _, _, _ -> },
            onExcursionClick = { _, _ -> }
        )
    }
}
