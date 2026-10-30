package llc.bokadev.kompass.presentation.search

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.search_activities
import kompass.composeapp.generated.resources.search_activity
import kompass.composeapp.generated.resources.search_all
import kompass.composeapp.generated.resources.search_browse_explore
import kompass.composeapp.generated.resources.search_chip_count
import kompass.composeapp.generated.resources.search_clear
import kompass.composeapp.generated.resources.search_clear_search
import kompass.composeapp.generated.resources.search_essential
import kompass.composeapp.generated.resources.search_essentials
import kompass.composeapp.generated.resources.search_event
import kompass.composeapp.generated.resources.search_events
import kompass.composeapp.generated.resources.search_incomplete
import kompass.composeapp.generated.resources.search_map_points
import kompass.composeapp.generated.resources.search_not_ready
import kompass.composeapp.generated.resources.search_nothing_body
import kompass.composeapp.generated.resources.search_nothing_for
import kompass.composeapp.generated.resources.search_on_map
import kompass.composeapp.generated.resources.search_one_result
import kompass.composeapp.generated.resources.search_place
import kompass.composeapp.generated.resources.search_places
import kompass.composeapp.generated.resources.search_read_or_listen
import kompass.composeapp.generated.resources.search_recent
import kompass.composeapp.generated.resources.search_results_count
import kompass.composeapp.generated.resources.search_retry
import kompass.composeapp.generated.resources.search_service
import kompass.composeapp.generated.resources.search_services
import kompass.composeapp.generated.resources.search_show_more
import kompass.composeapp.generated.resources.search_stories
import kompass.composeapp.generated.resources.search_story
import kompass.composeapp.generated.resources.search_suggestions_0
import kompass.composeapp.generated.resources.search_suggestions_1
import kompass.composeapp.generated.resources.search_suggestions_2
import kompass.composeapp.generated.resources.search_suggestions_3
import kompass.composeapp.generated.resources.search_suggestions_4
import kompass.composeapp.generated.resources.search_try_label
import kompass.composeapp.generated.resources.search_ui_placeholder
import kompass.composeapp.generated.resources.search_update
import kompass.composeapp.generated.resources.search_updates
import kotlinx.coroutines.delay
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.toDateTag
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.SearchHit
import llc.bokadev.kompass.domain.model.SearchIndexItem
import llc.bokadev.kompass.domain.model.SearchKind
import llc.bokadev.kompass.presentation.explore.components.ExploreGlyph
import llc.bokadev.kompass.presentation.explore.components.ExploreIcon
import llc.bokadev.kompass.presentation.shared.EssentialGroupKind
import llc.bokadev.kompass.presentation.shared.icons.EssentialsGlyph
import llc.bokadev.kompass.presentation.shared.icons.EssentialsIcon
import llc.bokadev.kompass.presentation.shared.icons.glyph
import llc.bokadev.kompass.presentation.shared.tileLabelRes
import llc.bokadev.kompass.presentation.shared.titleRes
import llc.bokadev.kompass.presentation.shared.labelRes
import llc.bokadev.kompass.presentation.shared.icons.LandmarkIcon
import llc.bokadev.kompass.presentation.search.components.SearchGlyph
import llc.bokadev.kompass.presentation.search.components.SearchGlyphIcon
import llc.bokadev.kompass.presentation.search.components.SearchHeader
import llc.bokadev.kompass.presentation.search.components.SearchResultRow
import llc.bokadev.kompass.presentation.search.components.SearchTypeChip
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource

/**
 * Lamplight Search: the field is the header; below it, Recent + Try before typing, then type
 * chips, a status line and result rows. Scrolling the results closes the keyboard.
 */
@Composable
fun SearchScreenContent(
    modifier: Modifier = Modifier,
    state: SearchState,
    listState: LazyListState,
    focusRequester: FocusRequester,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onQuerySubmit: (String) -> Unit,
    onKindChange: (SearchKind?) -> Unit,
    onResultClick: (SearchHit) -> Unit,
    onShowMoreClick: () -> Unit,
    onClearRecentClick: () -> Unit,
    onRetryClick: () -> Unit,
    onBrowseExploreClick: () -> Unit
) {
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val keyboard = LocalSoftwareKeyboardController.current
    val hideKeyboardOnDrag = remember(keyboard) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) keyboard?.hide()
                return Offset.Zero
            }
        }
    }

    Column(modifier.fillMaxSize()) {
        SearchHeader(
            query = state.query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(Res.string.search_ui_placeholder),
            backLabel = stringResource(Res.string.detail_back),
            clearLabel = stringResource(Res.string.search_clear_search),
            onBack = onBackClick,
            onSearchAction = {
                keyboard?.hide()
                onQuerySubmit(state.query)
            },
            focusRequester = focusRequester
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().nestedScroll(hideKeyboardOnDrag),
            contentPadding = PaddingValues(bottom = 26.dp)
        ) {
            when {
                state.indexStatus == SearchIndexStatus.NOT_READY -> item(key = "not-ready") { NotReady(onRetryClick) }
                !state.isSearching -> {
                    if (state.recent.isNotEmpty()) {
                        item(key = "recent-h") { RecentHeader(onClearRecentClick) }
                        items(state.recent, key = { "recent-$it" }) { RecentRow(it) { onQuerySubmit(it) } }
                    }
                    item(key = "try") { TrySuggestions(lang, onQuerySubmit) }
                }
                state.indexStatus == SearchIndexStatus.LOADING -> Unit
                state.hits.isEmpty() -> {
                    if (state.isIncomplete) item(key = "incomplete") { IncompleteNotice(onRetryClick) }
                    item(key = "none") { NoResults(state.searchedQuery.trim(), onBrowseExploreClick) }
                }
                else -> {
                    if (state.isIncomplete) item(key = "incomplete") { IncompleteNotice(onRetryClick) }
                    item(key = "chips") { TypeChips(state, onKindChange) }
                    item(key = "status") { StatusLine(state) }
                    items(state.visible, key = { "${it.item.kind}-${it.item.id}" }) { hit ->
                        ResultRow(hit, lang) { onResultClick(hit) }
                    }
                    if (state.hasMore) {
                        item(key = "more") {
                            Text(
                                text = stringResource(Res.string.search_show_more),
                                style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.SemiBold),
                                color = KompassTheme.colors.lampYellow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onShowMoreClick)
                                    .semantics { role = Role.Button }
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = KompassTheme.typography.geologicaSemiBold11, color = KompassTheme.colors.pebbleGray, modifier = modifier.semantics { heading() })
}

@Composable
private fun RecentHeader(onClear: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionLabel(stringResource(Res.string.search_recent), Modifier.weight(1f))
        Box(
            Modifier
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onClear)
                .semantics { role = Role.Button }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(Res.string.search_clear), style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold), color = KompassTheme.colors.lampYellow)
        }
    }
}

@Composable
private fun RecentRow(query: String, onClick: () -> Unit) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SearchGlyphIcon(SearchGlyph.Clock, colors.pebbleGray, 18.dp)
        Text(query, style = KompassTheme.typography.geologicaMedium16, color = colors.boneWhite, maxLines = 1)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrySuggestions(lang: String, onRun: (String) -> Unit) {
    val colors = KompassTheme.colors
    // Editorial "Try" suggestions. Never generated, never trending.
    val suggestions = listOf(
        stringResource(Res.string.search_suggestions_0),
        stringResource(Res.string.search_suggestions_1),
        stringResource(Res.string.search_suggestions_2),
        stringResource(Res.string.search_suggestions_3),
        stringResource(Res.string.search_suggestions_4)
    )
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(stringResource(Res.string.search_try_label).uppercaseFor(lang))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { suggestion ->
                Box(
                    Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, colors.slateGray, RoundedCornerShape(20.dp))
                        .clickable { onRun(suggestion) }
                        .semantics { role = Role.Button }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) { Text(suggestion, style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.Medium), color = colors.boneWhite) }
            }
        }
    }
}

@Composable
private fun TypeChips(state: SearchState, onKind: (SearchKind?) -> Unit) {
    val all = stringResource(Res.string.search_all)
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchTypeChip(
            all, state.hits.size, state.selectedKind == null,
            stringResource(Res.string.search_chip_count, all, state.hits.size.toString()), { onKind(null) }
        )
        state.kindCounts.forEach { (kind, n) ->
            val label = kind.pluralLabel()
            SearchTypeChip(label, n, state.selectedKind == kind, stringResource(Res.string.search_chip_count, label, n.toString()), { onKind(kind) })
        }
    }
}

@Composable
private fun StatusLine(state: SearchState) {
    val n = state.filtered.size
    val text = if (n == 1) stringResource(Res.string.search_one_result) else stringResource(Res.string.search_results_count, n.toString())
    // Announced only after a 600ms typing pause, not on every keystroke.
    var announced by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        delay(600)
        announced = text
    }
    Box(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp)) {
        Text(text, style = KompassTheme.typography.geologicaRegular13, color = KompassTheme.colors.pebbleGray)
        Box(Modifier.size(1.dp).semantics { liveRegion = LiveRegionMode.Polite; contentDescription = announced })
    }
}

@Composable
private fun ResultRow(hit: SearchHit, lang: String, onClick: () -> Unit) {
    val item = hit.item
    val onMapLabel = item.pointCount?.let { stringResource(Res.string.search_on_map, it.toString()) }
    SearchResultRow(
        typeLabel = (item.utilityCategory?.takeIf { item.pointCount == null }?.let { stringResource(it.tileLabelRes()) } ?: item.kind.singularLabel()).uppercaseFor(lang),
        name = item.name,
        matchStart = hit.start,
        matchLength = hit.length,
        context = item.contextLine(lang, stringResource(Res.string.search_read_or_listen), onMapLabel),
        photoUrl = item.photo?.let(::buildPhotoUrl),
        essential = item.kind == SearchKind.ESSENTIAL,
        icon = { color -> KindIcon(item, color) },
        chevron = { color -> ExploreIcon(ExploreGlyph.ChevronRight, color, 16.dp) },
        onClick = onClick
    )
}

@Composable
private fun KindIcon(item: SearchIndexItem, color: Color) = when (item.kind) {
    SearchKind.PLACE -> LandmarkIcon(color, 24.dp)
    SearchKind.ACTIVITY -> ExploreIcon(ExploreGlyph.Walker, color, 24.dp)
    SearchKind.STORY -> SearchGlyphIcon(SearchGlyph.Book, color, 24.dp)
    SearchKind.ESSENTIAL -> ExploreIcon(ExploreGlyph.Info, color, 24.dp)
    SearchKind.EVENT -> ExploreIcon(ExploreGlyph.Calendar, color, 24.dp)
    SearchKind.UTILITY -> EssentialsIcon(item.utilityCategory?.glyph() ?: EssentialsGlyph.Pin, color, 24.dp)
    SearchKind.SERVICE -> ExploreIcon(ExploreGlyph.Wrench, color, 24.dp)
    SearchKind.NOTICE -> EssentialsIcon(EssentialsGlyph.Warning, color, 24.dp)
}

/** Some sources failed and weren't cached: say so, with Retry, above results or "nothing". */
@Composable
private fun IncompleteNotice(onRetry: () -> Unit) {
    val colors = KompassTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        EssentialsIcon(EssentialsGlyph.Warning, colors.lampYellow, 18.dp)
        Text(
            text = stringResource(Res.string.search_incomplete),
            style = KompassTheme.typography.geologicaRegular14,
            color = KompassTheme.colors.white85,
            modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }
        )
        Box(
            Modifier
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onRetry)
                .semantics { role = Role.Button }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(Res.string.search_retry), style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold), color = colors.lampYellow)
        }
    }
}

@Composable
private fun NoResults(query: String, onBrowseExplore: () -> Unit) {
    val colors = KompassTheme.colors
    val title = stringResource(Res.string.search_nothing_for, query)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchGlyphIcon(SearchGlyph.SearchMinus, colors.lampYellow, 44.dp)
        Text(
            text = title,
            style = KompassTheme.typography.gloockRegular26,
            color = colors.boneWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite; heading() }
        )
        Text(
            text = stringResource(Res.string.search_nothing_body),
            style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal),
            color = KompassTheme.colors.white85,
            textAlign = TextAlign.Center
        )
        OutlinePill(stringResource(Res.string.search_browse_explore), onBrowseExplore, Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun NotReady(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.search_not_ready),
            style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 23.sp),
            color = KompassTheme.colors.white85,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        OutlinePill(stringResource(Res.string.search_retry), onRetry)
    }
}

@Composable
private fun OutlinePill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = KompassTheme.colors
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, colors.slateGray, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.SemiBold), color = colors.boneWhite) }
}

@Composable
private fun SearchKind.pluralLabel(): String = when (this) {
    SearchKind.PLACE -> stringResource(Res.string.search_places)
    SearchKind.ACTIVITY -> stringResource(Res.string.search_activities)
    SearchKind.STORY -> stringResource(Res.string.search_stories)
    SearchKind.ESSENTIAL -> stringResource(Res.string.search_essentials)
    SearchKind.EVENT -> stringResource(Res.string.search_events)
    SearchKind.UTILITY -> stringResource(Res.string.search_map_points)
    SearchKind.SERVICE -> stringResource(Res.string.search_services)
    SearchKind.NOTICE -> stringResource(Res.string.search_updates)
}

@Composable
private fun SearchKind.singularLabel(): String = when (this) {
    SearchKind.PLACE -> stringResource(Res.string.search_place)
    SearchKind.ACTIVITY -> stringResource(Res.string.search_activity)
    SearchKind.STORY -> stringResource(Res.string.search_story)
    SearchKind.ESSENTIAL -> stringResource(Res.string.search_essential)
    SearchKind.EVENT -> stringResource(Res.string.search_event)
    SearchKind.UTILITY -> stringResource(Res.string.search_map_points)
    SearchKind.SERVICE -> stringResource(Res.string.search_service)
    SearchKind.NOTICE -> stringResource(Res.string.search_update)
}

/** `{subtype} · {area}` for places/activities, the group for Essentials, "Read or listen" for stories. */
@Composable
private fun SearchIndexItem.contextLine(lang: String, readOrListen: String, onMapLabel: String?): String? = when (kind) {
    SearchKind.PLACE -> listOfNotNull(subtype ?: placeCategory?.let { stringResource(it.labelRes()) }, area).joinToString(" · ")
    SearchKind.ACTIVITY -> listOfNotNull(subtype, area).joinToString(" · ")
    SearchKind.STORY -> readOrListen
    SearchKind.ESSENTIAL -> EssentialGroupKind.entries.firstOrNull { it.category == essentialCategory }?.let { stringResource(it.titleRes()) }
    SearchKind.EVENT -> listOfNotNull(eventStart?.toDateTag(lang), area).joinToString(" · ")
    SearchKind.UTILITY -> onMapLabel ?: area
    SearchKind.SERVICE, SearchKind.NOTICE -> area
}?.takeIf { it.isNotBlank() }

@Preview
@Composable
private fun SearchScreenContentPreview() {
    KompassTheme {
        SearchScreenContent(
            modifier = Modifier.pageGradientBackground(),
            state = SearchState(
                query = "pi",
                searchedQuery = "pi",
                hits = listOf(
                    SearchHit(SearchIndexItem(SearchKind.PLACE, "pima", "Pima Palace", area = "Old Town"), score = 0, start = 0, length = 2),
                    SearchHit(SearchIndexItem(SearchKind.STORY, "spirit", "The spirit of Pima", area = "Old Town"), score = 1, start = 14, length = 2)
                ),
                recent = listOf("perast"),
                indexStatus = SearchIndexStatus.READY
            ),
            listState = rememberLazyListState(),
            focusRequester = remember { FocusRequester() },
            onBackClick = {},
            onQueryChange = {},
            onQuerySubmit = {},
            onKindChange = {},
            onResultClick = {},
            onShowMoreClick = {},
            onClearRecentClick = {},
            onRetryClick = {},
            onBrowseExploreClick = {}
        )
    }
}
