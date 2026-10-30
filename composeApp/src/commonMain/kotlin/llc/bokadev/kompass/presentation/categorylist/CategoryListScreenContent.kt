package llc.bokadev.kompass.presentation.categorylist

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.activities_title
import kompass.composeapp.generated.resources.category_list_all_areas
import kompass.composeapp.generated.resources.category_list_area_all
import kompass.composeapp.generated.resources.category_list_area_selected
import kompass.composeapp.generated.resources.category_list_clear_filter
import kompass.composeapp.generated.resources.category_list_count_activities
import kompass.composeapp.generated.resources.category_list_count_places
import kompass.composeapp.generated.resources.category_list_couldnt_refresh
import kompass.composeapp.generated.resources.category_list_no_match
import kompass.composeapp.generated.resources.category_list_search
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.redesign_activities_subtitle
import kompass.composeapp.generated.resources.redesign_food
import kompass.composeapp.generated.resources.redesign_food_subtitle
import kompass.composeapp.generated.resources.redesign_landmarks
import kompass.composeapp.generated.resources.redesign_must_see_saved
import kompass.composeapp.generated.resources.redesign_not_saved
import kompass.composeapp.generated.resources.redesign_places
import kompass.composeapp.generated.resources.redesign_places_subtitle
import kompass.composeapp.generated.resources.redesign_remove_title_from_saved
import kompass.composeapp.generated.resources.redesign_save_title
import kompass.composeapp.generated.resources.redesign_whole_bay
import kompass.composeapp.generated.resources.retry
import kompass.composeapp.generated.resources.saved_activity
import kompass.composeapp.generated.resources.saved_audio
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.localizedTypeLabel
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.CategoryListKind
import llc.bokadev.kompass.presentation.categorylist.components.CategoryListCard
import llc.bokadev.kompass.presentation.screens.essentials.formatDistance
import llc.bokadev.kompass.presentation.screens.home.components.ChevronDownIcon
import llc.bokadev.kompass.presentation.shared.LamplightCompactHeader
import llc.bokadev.kompass.presentation.shared.areaLabel
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** The shared Lamplight category list. Rows keep their order when saved; only their bookmark changes. */
@Composable
fun CategoryListScreenContent(
    modifier: Modifier = Modifier,
    state: CategoryListState,
    listState: LazyListState,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAreaChange: (String?) -> Unit,
    onItemClick: (ListItem) -> Unit,
    onSavedChange: (ListItem, Boolean) -> Unit,
    onRetryClick: () -> Unit
) {
    val activities = state.kind == CategoryListKind.ACTIVITIES

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 26.dp)
    ) {
        item(key = "top") {
            LamplightCompactHeader(
                title = stringResource(state.kind.title()),
                subtitle = stringResource(state.kind.subtitle()),
                backLabel = stringResource(Res.string.detail_back),
                onBack = onBackClick,
                searchLabel = stringResource(Res.string.category_list_search),
                onSearch = onSearchClick
            )
        }
        item(key = "list-header") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (activities) Res.string.category_list_count_activities else Res.string.category_list_count_places,
                        state.count.toString()
                    ),
                    style = KompassTheme.typography.geologicaRegular14,
                    color = KompassTheme.colors.white85,
                    modifier = Modifier.weight(1f)
                )
                AreaFilter(state, onAreaChange)
            }
        }
        items(state.items, key = { "${it.type}-${it.id}" }) { item ->
            Card(item, saved = state.isSaved(item), onItemClick, onSavedChange)
        }
        if (!state.isLoading && state.count == 0) {
            item(key = "empty") { EmptyOrError(state, onRetryClick, onClearFilter = { onAreaChange(null) }) }
        }
        if (state.refreshFailed) {
            item(key = "refresh-failed") {
                Text(
                    text = stringResource(Res.string.category_list_couldnt_refresh),
                    style = KompassTheme.typography.geologicaRegular13,
                    color = KompassTheme.colors.white85,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}

@Composable
private fun Card(
    item: ListItem,
    saved: Boolean,
    onItemClick: (ListItem) -> Unit,
    onSavedChange: (ListItem, Boolean) -> Unit
) {
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val title = item.place?.localizedName(lang) ?: item.activity?.localizedName(lang).orEmpty()
    // localizedTypeLabel still takes the AppStrings tables until it is migrated.
    val type = item.place?.localizedTypeLabel(appStringsFor(lang)) ?: stringResource(Res.string.saved_activity)
    val label = listOfNotNull(type, item.areaKey.areaLabel()).joinToString(" · ").uppercaseFor(lang)
    val hasAudio = item.place?.localizedAudioFile(lang) != null || item.activity?.localizedAudioFile(lang) != null
    val photo = item.place?.photos?.firstOrNull() ?: item.activity?.photos?.firstOrNull()
    Box(Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
        CategoryListCard(
            title = title,
            label = label,
            imageUrl = photo?.let(::buildPhotoUrl),
            meta = item.distanceKm?.let { (it * 1000).roundToInt().formatDistance() },
            hasAudio = hasAudio,
            audioLabel = stringResource(Res.string.saved_audio),
            saved = saved,
            saveDescription = stringResource(if (saved) Res.string.redesign_remove_title_from_saved else Res.string.redesign_save_title, title),
            savedStateLabel = stringResource(if (saved) Res.string.redesign_must_see_saved else Res.string.redesign_not_saved),
            onToggle = { onSavedChange(item, !saved) },
            onOpen = { onItemClick(item) }
        )
    }
}

@Composable
private fun AreaFilter(state: CategoryListState, onArea: (String?) -> Unit) {
    val colors = KompassTheme.colors
    var open by remember { mutableStateOf(false) }
    val active = state.areaFilter
    Box {
        Row(
            modifier = Modifier
                .heightIn(min = 44.dp)
                .clip(KompassShapes.pill)
                .border(1.5.dp, if (active != null) colors.lampYellow else colors.slateGray, KompassShapes.pill)
                .clickable { if (active != null) onArea(null) else open = true }
                .semantics(mergeDescendants = true) { role = Role.Button }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = active?.let { stringResource(Res.string.category_list_area_selected, it.areaLabel()) }
                    ?: stringResource(Res.string.category_list_area_all),
                style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
                color = colors.boneWhite
            )
            // An active filter shows ✕ (tap clears it); otherwise a chevron opens the menu.
            if (active != null) Text(text = "✕", style = KompassTheme.typography.geologicaRegular14, color = colors.lampYellow)
            else ChevronDownIcon(color = colors.boneWhite, size = 14.dp)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text(stringResource(Res.string.category_list_all_areas)) }, onClick = { open = false; onArea(null) })
            state.areas.forEach { key ->
                DropdownMenuItem(text = { Text(key.areaLabel()) }, onClick = { open = false; onArea(key) })
            }
        }
    }
}

@Composable
private fun EmptyOrError(state: CategoryListState, onRetryClick: () -> Unit, onClearFilter: () -> Unit) {
    val colors = KompassTheme.colors
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val error = state.error
        val retry = error != null
        Text(
            text = stringResource(error?.messageRes ?: Res.string.category_list_no_match),
            style = KompassTheme.typography.geologicaMedium16,
            color = KompassTheme.colors.white85
        )
        if (retry || state.areaFilter != null) {
            Text(
                text = stringResource(if (retry) Res.string.retry else Res.string.category_list_clear_filter),
                style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.SemiBold),
                color = colors.nightBlack,
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .clip(KompassShapes.pill)
                    .background(colors.lampYellow)
                    .clickable { if (retry) onRetryClick() else onClearFilter() }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

private fun CategoryListKind.title(): StringResource = when (this) {
    CategoryListKind.PLACES -> Res.string.redesign_places
    CategoryListKind.EAT_AND_DRINK -> Res.string.redesign_food
    CategoryListKind.ACTIVITIES -> Res.string.activities_title
    CategoryListKind.MUST_SEE -> Res.string.redesign_landmarks
}

private fun CategoryListKind.subtitle(): StringResource = when (this) {
    CategoryListKind.PLACES -> Res.string.redesign_places_subtitle
    CategoryListKind.EAT_AND_DRINK -> Res.string.redesign_food_subtitle
    CategoryListKind.ACTIVITIES -> Res.string.redesign_activities_subtitle
    CategoryListKind.MUST_SEE -> Res.string.redesign_whole_bay
}

@Preview
@Composable
private fun CategoryListScreenContentPreview() {
    KompassTheme {
        CategoryListScreenContent(
            modifier = Modifier.pageGradientBackground(),
            state = CategoryListState(isLoading = false, kind = CategoryListKind.PLACES, areas = listOf("old_town", "perast")),
            listState = rememberLazyListState(),
            onBackClick = {},
            onSearchClick = {},
            onAreaChange = {},
            onItemClick = {},
            onSavedChange = { _, _ -> },
            onRetryClick = {}
        )
    }
}
