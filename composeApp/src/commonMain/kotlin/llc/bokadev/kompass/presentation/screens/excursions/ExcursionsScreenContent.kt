package llc.bokadev.kompass.presentation.screens.excursions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.utils.*
import llc.bokadev.kompass.domain.model.*
import llc.bokadev.kompass.core.presentation.mvi.asString
import llc.bokadev.kompass.presentation.screens.excursions.components.ExcursionRow
import llc.bokadev.kompass.ui.theme.KompassTheme

/** Worth the Drive: an editorial collection with interest/time/planning filters. */
@Composable
fun ExcursionsScreenContent(
    state: ExcursionsState,
    onAction: (ExcursionsAction) -> Unit
) {
    val app = rememberAppStrings()
    val strings = app.excursions
    val colors = KompassTheme.colors
    val filter = state.filter
    val visible = state.items.filter(filter::matches)
    Column(Modifier.fillMaxSize().background(colors.nightBlack).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            TextButton(onClick = { onAction(ExcursionsAction.Back) }) { Text(strings.back, color = colors.lampYellow) }
        }
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item {
                Text(app.redesign.worthDrive, style = KompassTheme.typography.gloockRegular40, color = colors.boneWhite)
                Spacer(Modifier.height(10.dp))
                Text(strings.intro, style = KompassTheme.typography.geologicaMedium16, color = colors.stoneGray)
            }
            if (state.items.isNotEmpty()) {
                item {
                    FilterGroup(strings.interests, listOf("culture", "nature", "hiking", "water").filter { key -> state.items.any { key in it.interests } }, filter.interests) { key ->
                        onAction(ExcursionsAction.Filter(filter.copy(interests = if (key in filter.interests) filter.interests - key else filter.interests + key)))
                    }
                    FilterGroup(strings.time, listOf("short", "half_day", "full_day").filter { key -> state.items.any { it.timeBand == key } }, setOfNotNull(filter.timeBand)) { key ->
                        onAction(ExcursionsAction.Filter(filter.copy(timeBand = key.takeUnless { it == filter.timeBand })))
                    }
                    FilterGroup(strings.planning, listOf("independent", "arrange_ahead").filter { key -> state.items.any { it.planningLevel == key } }, setOfNotNull(filter.planningLevel)) { key ->
                        onAction(ExcursionsAction.Filter(filter.copy(planningLevel = key.takeUnless { it == filter.planningLevel })))
                    }
                }
            }
            if (filter != ExcursionFilter()) item {
                TextButton(onClick = { onAction(ExcursionsAction.Filter(ExcursionFilter())) }) { Text(strings.reset, color = colors.lampYellow) }
            }
            if (state.cached) item {
                Text(strings.cached, color = colors.stoneGray, style = KompassTheme.typography.geologicaMedium16)
                TextButton(onClick = { onAction(ExcursionsAction.Reload) }) { Text(app.retry, color = colors.lampYellow) }
            }
            when {
                state.isLoading -> item { CircularProgressIndicator(color = colors.lampYellow) }
                state.error != null -> item {
                    Text(state.error.asString(), color = colors.boneWhite)
                    TextButton(onClick = { onAction(ExcursionsAction.Reload) }) { Text(app.retry, color = colors.lampYellow) }
                }
                visible.isEmpty() -> item { Text(if (state.items.isEmpty()) strings.empty else strings.noMatches, color = colors.stoneGray) }
            }
            items(visible, key = { it.id }) { entry ->
                val type = if (entry.place != null) FavoriteItemType.PLACE else FavoriteItemType.ACTIVITY
                val id = entry.place?.id ?: entry.activity!!.id
                val saved = FavoriteKey(type, id) in state.savedKeys
                ExcursionRow(entry, saved, { onAction(ExcursionsAction.SetSaved(type, id, !saved)) }) {
                    onAction(ExcursionsAction.Open(type, id))
                }
            }
        }
    }
}

@Composable
private fun FilterGroup(title: String, values: List<String>, selected: Set<String>, onClick: (String) -> Unit) {
    if (values.isEmpty()) return
    val colors = KompassTheme.colors
    val strings = rememberAppStrings().excursions
    Text(title, style = KompassTheme.typography.geologicaMedium16, color = colors.stoneGray)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(values) { value -> FilterChip(
            selected = value in selected, onClick = { onClick(value) }, label = { Text(strings.label(value)) },
            colors = FilterChipDefaults.filterChipColors(containerColor = colors.charcoal, labelColor = colors.boneWhite,
                selectedContainerColor = colors.lampYellow, selectedLabelColor = colors.nightBlack),
            modifier = Modifier.heightIn(min = 48.dp)
        ) }
    }
}
