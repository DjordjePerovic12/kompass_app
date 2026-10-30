package llc.bokadev.kompass.presentation.screens.categorylist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.presentation.mvi.asString
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.localizedTypeLabel
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.presentation.screens.categorylist.components.CategoryListCard
import llc.bokadev.kompass.presentation.screens.essentials.formatDistance
import llc.bokadev.kompass.presentation.screens.favorites.GROUP_OTHER
import llc.bokadev.kompass.presentation.screens.home.HomeArea
import llc.bokadev.kompass.presentation.screens.home.components.ChevronDownIcon
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.presentation.theme.HomePalette
import llc.bokadev.kompass.presentation.theme.LamplightShapes
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import llc.bokadev.kompass.presentation.theme.pageGradientBackground
import kotlin.math.roundToInt

/** The shared Lamplight category list. Rows keep their order when saved; only their bookmark changes. */
@Composable
fun CategoryListScreenContent(
    state: CategoryListState,
    listState: LazyListState,
    onAction: (CategoryListAction) -> Unit
) {
    val strings = rememberAppStrings()
    val lang = currentAppLanguage()
    val colors = LamplightTheme.colors
    val activities = state.kind == CategoryListKind.ACTIVITIES
    val c = strings.categoryList

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().pageGradientBackground(),
        contentPadding = PaddingValues(bottom = 26.dp)
    ) {
        item(key = "top") {
            LamplightCompactHeader(
                title = state.kind.title(strings),
                subtitle = state.kind.subtitle(strings),
                backLabel = strings.detail.back,
                onBack = { onAction(CategoryListAction.Back) },
                searchLabel = c.search,
                onSearch = { onAction(CategoryListAction.OpenSearch) }
            )
        }
        item(key = "list-header") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (if (activities) c.countActivities else c.countPlaces).replace("{n}", state.count.toString()),
                    style = LamplightType.bodyS(),
                    color = HomePalette.onGradientSecondary,
                    modifier = Modifier.weight(1f)
                )
                AreaFilter(state, strings) { onAction(CategoryListAction.SetArea(it)) }
            }
        }
        items(state.items, key = { "${it.type}-${it.id}" }) { item ->
            Card(item, saved = state.isSaved(item), lang, strings, onAction)
        }
        if (!state.isLoading && state.count == 0) {
            item(key = "empty") { EmptyOrError(state, strings, onAction) }
        }
        if (state.refreshFailed) {
            item(key = "refresh-failed") {
                Text(text = c.couldntRefresh, style = LamplightType.meta(), color = HomePalette.onGradientSecondary, modifier = Modifier.padding(20.dp))
            }
        }
    }
}

@Composable
private fun Card(
    item: ListItem,
    saved: Boolean,
    lang: String,
    strings: AppStrings,
    onAction: (CategoryListAction) -> Unit
) {
    val title = item.place?.localizedName(lang) ?: item.activity?.localizedName(lang).orEmpty()
    val type = item.place?.localizedTypeLabel(strings) ?: strings.saved.activity
    val label = listOfNotNull(type, item.areaKey.areaLabel(strings)).joinToString(" · ").uppercaseFor(lang)
    val hasAudio = item.place?.localizedAudioFile(lang) != null || item.activity?.localizedAudioFile(lang) != null
    val photo = item.place?.photos?.firstOrNull() ?: item.activity?.photos?.firstOrNull()
    Box(Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
        CategoryListCard(
            title = title,
            label = label,
            imageUrl = photo?.let(::buildPhotoUrl),
            meta = item.distanceKm?.let { (it * 1000).roundToInt().formatDistance() },
            hasAudio = hasAudio,
            audioLabel = strings.saved.audio,
            saved = saved,
            saveDescription = (if (saved) strings.redesign.removeTitleFromSaved else strings.redesign.saveTitle).replace("{title}", title),
            savedStateLabel = if (saved) strings.redesign.mustSeeSaved else strings.redesign.notSaved,
            onToggle = { onAction(CategoryListAction.SetSaved(item, saved = !saved)) },
            onOpen = { onAction(CategoryListAction.Open(item)) }
        )
    }
}

@Composable
private fun AreaFilter(state: CategoryListState, strings: AppStrings, onArea: (String?) -> Unit) {
    val colors = LamplightTheme.colors
    var open by remember { mutableStateOf(false) }
    val active = state.areaFilter
    Box {
        Row(
            modifier = Modifier
                .heightIn(min = 44.dp)
                .clip(LamplightShapes.pill)
                .border(1.5.dp, if (active != null) colors.lamp else colors.borderStrong, LamplightShapes.pill)
                .clickable { if (active != null) onArea(null) else open = true }
                .semantics(mergeDescendants = true) { role = Role.Button }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = active?.let { strings.categoryList.areaSelected.replace("{area}", it.areaLabel(strings)) } ?: strings.categoryList.areaAll,
                style = LamplightType.bodyS().copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary
            )
            // An active filter shows ✕ (tap clears it); otherwise a chevron opens the menu.
            if (active != null) Text(text = "✕", style = LamplightType.bodyS(), color = colors.lamp)
            else ChevronDownIcon(color = colors.textPrimary, size = 14.dp)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text(strings.categoryList.allAreas) }, onClick = { open = false; onArea(null) })
            state.areas.forEach { key ->
                DropdownMenuItem(text = { Text(key.areaLabel(strings)) }, onClick = { open = false; onArea(key) })
            }
        }
    }
}

@Composable
private fun EmptyOrError(state: CategoryListState, strings: AppStrings, onAction: (CategoryListAction) -> Unit) {
    val colors = LamplightTheme.colors
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val retry = state.error != null
        Text(
            text = state.error?.asString() ?: strings.categoryList.noMatch,
            style = LamplightType.body(),
            color = HomePalette.onGradientSecondary
        )
        if (retry || state.areaFilter != null) {
            Text(
                text = if (retry) strings.retry else strings.categoryList.clearFilter,
                style = LamplightType.body().copy(fontWeight = FontWeight.SemiBold),
                color = colors.night,
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .clip(LamplightShapes.pill)
                    .background(colors.lamp)
                    .clickable { onAction(if (retry) CategoryListAction.Retry else CategoryListAction.SetArea(null)) }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

private fun CategoryListKind.title(strings: AppStrings): String = when (this) {
    CategoryListKind.PLACES -> strings.redesign.places
    CategoryListKind.EAT_AND_DRINK -> strings.redesign.food
    CategoryListKind.ACTIVITIES -> strings.activitiesTitle
    CategoryListKind.MUST_SEE -> strings.redesign.landmarks
}

private fun CategoryListKind.subtitle(strings: AppStrings): String = when (this) {
    CategoryListKind.PLACES -> strings.redesign.placesSubtitle
    CategoryListKind.EAT_AND_DRINK -> strings.redesign.foodSubtitle
    CategoryListKind.ACTIVITIES -> strings.redesign.activitiesSubtitle
    CategoryListKind.MUST_SEE -> strings.redesign.wholeBay
}

internal fun String.areaLabel(strings: AppStrings): String = when (this) {
    GROUP_OTHER -> strings.saved.groupOther
    else -> HomeArea.entries.firstOrNull { it.preferenceValue == this }?.shortLabel(strings) ?: toTitleCaseLabel()
}
