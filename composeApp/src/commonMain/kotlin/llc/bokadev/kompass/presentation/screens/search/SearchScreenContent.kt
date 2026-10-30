package llc.bokadev.kompass.presentation.screens.search

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.localizedLabel
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.core.utils.toDateTag
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.SearchHit
import llc.bokadev.kompass.domain.model.SearchIndexItem
import llc.bokadev.kompass.domain.model.SearchKind
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreGlyph
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreIcon
import llc.bokadev.kompass.presentation.screens.essentials.EssentialGroupKind
import llc.bokadev.kompass.presentation.screens.essentials.components.EssentialsGlyph
import llc.bokadev.kompass.presentation.screens.essentials.components.EssentialsIcon
import llc.bokadev.kompass.presentation.screens.essentials.components.glyph
import llc.bokadev.kompass.presentation.screens.essentials.tileLabel
import llc.bokadev.kompass.presentation.screens.essentials.title
import llc.bokadev.kompass.presentation.screens.home.components.LandmarkIcon
import llc.bokadev.kompass.presentation.screens.search.components.SearchGlyph
import llc.bokadev.kompass.presentation.screens.search.components.SearchGlyphIcon
import llc.bokadev.kompass.presentation.screens.search.components.SearchHeader
import llc.bokadev.kompass.presentation.screens.search.components.SearchResultRow
import llc.bokadev.kompass.presentation.screens.search.components.SearchTypeChip
import llc.bokadev.kompass.presentation.theme.HomePalette
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import llc.bokadev.kompass.presentation.theme.pageGradientBackground

private data class SearchActions(
    val onBack: () -> Unit,
    val onQueryChange: (String) -> Unit,
    val onRunQuery: (String) -> Unit,
    val onSearchAction: () -> Unit,
    val onKind: (SearchKind?) -> Unit,
    val onOpen: (SearchHit) -> Unit,
    val onShowMore: () -> Unit,
    val onClearRecent: () -> Unit,
    val onRetry: () -> Unit,
    val onBrowseExplore: () -> Unit
)

/**
 * Lamplight Search: the field is the header; below it, Recent + Try before typing, then type
 * chips, a status line and result rows. Scrolling the results closes the keyboard.
 */
@Composable
fun SearchScreenContent(
    state: SearchState,
    listState: LazyListState,
    focusRequester: FocusRequester,
    onAction: (SearchAction) -> Unit
) {
    val strings = rememberAppStrings()
    val s = strings.search
    val lang = currentAppLanguage()
    val keyboard = LocalSoftwareKeyboardController.current
    val actions = SearchActions(
        onBack = { onAction(SearchAction.Back) },
        onQueryChange = { onAction(SearchAction.QueryChanged(it)) },
        onRunQuery = { onAction(SearchAction.RunQuery(it)) },
        onSearchAction = {
            keyboard?.hide()
            onAction(SearchAction.RunQuery(state.query))
        },
        onKind = { onAction(SearchAction.SelectKind(it)) },
        onOpen = { onAction(SearchAction.Open(it)) },
        onShowMore = { onAction(SearchAction.ShowMore) },
        onClearRecent = { onAction(SearchAction.ClearRecent) },
        onRetry = { onAction(SearchAction.Retry) },
        onBrowseExplore = { onAction(SearchAction.BrowseExplore) }
    )
    val hideKeyboardOnDrag = remember(keyboard) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) keyboard?.hide()
                return Offset.Zero
            }
        }
    }

    Column(Modifier.fillMaxSize().pageGradientBackground()) {
        SearchHeader(
            query = state.query,
            onQueryChange = actions.onQueryChange,
            placeholder = s.placeholder,
            backLabel = strings.detail.back,
            clearLabel = s.clearSearch,
            onBack = actions.onBack,
            onSearchAction = actions.onSearchAction,
            focusRequester = focusRequester
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().nestedScroll(hideKeyboardOnDrag),
            contentPadding = PaddingValues(bottom = 26.dp)
        ) {
            when {
                state.indexStatus == SearchIndexStatus.NOT_READY -> item(key = "not-ready") { NotReady(strings, actions.onRetry) }
                !state.isSearching -> {
                    if (state.recent.isNotEmpty()) {
                        item(key = "recent-h") { RecentHeader(strings, actions.onClearRecent) }
                        items(state.recent, key = { "recent-$it" }) { RecentRow(it) { actions.onRunQuery(it) } }
                    }
                    item(key = "try") { TrySuggestions(strings, lang, actions.onRunQuery) }
                }
                state.indexStatus == SearchIndexStatus.LOADING -> Unit
                state.hits.isEmpty() -> {
                    if (state.isIncomplete) item(key = "incomplete") { IncompleteNotice(strings, actions.onRetry) }
                    item(key = "none") { NoResults(state.searchedQuery.trim(), strings, actions.onBrowseExplore) }
                }
                else -> {
                    if (state.isIncomplete) item(key = "incomplete") { IncompleteNotice(strings, actions.onRetry) }
                    item(key = "chips") { TypeChips(state, strings, actions.onKind) }
                    item(key = "status") { StatusLine(state, strings) }
                    items(state.visible, key = { "${it.item.kind}-${it.item.id}" }) { hit ->
                        ResultRow(hit, strings, lang) { actions.onOpen(hit) }
                    }
                    if (state.hasMore) {
                        item(key = "more") {
                            Text(
                                text = s.showMore,
                                style = LamplightType.body().copy(fontWeight = FontWeight.SemiBold),
                                color = LamplightTheme.colors.lamp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = actions.onShowMore)
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
    Text(text = text, style = LamplightType.label(), color = LamplightTheme.colors.textTertiary, modifier = modifier.semantics { heading() })
}

@Composable
private fun RecentHeader(strings: AppStrings, onClear: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionLabel(strings.search.recent, Modifier.weight(1f))
        Box(
            Modifier
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onClear)
                .semantics { role = Role.Button }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(strings.search.clear, style = LamplightType.bodyS().copy(fontWeight = FontWeight.SemiBold), color = LamplightTheme.colors.lamp)
        }
    }
}

@Composable
private fun RecentRow(query: String, onClick: () -> Unit) {
    val colors = LamplightTheme.colors
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
        SearchGlyphIcon(SearchGlyph.Clock, colors.textTertiary, 18.dp)
        Text(query, style = LamplightType.body(), color = colors.textPrimary, maxLines = 1)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrySuggestions(strings: AppStrings, lang: String, onRun: (String) -> Unit) {
    val colors = LamplightTheme.colors
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(strings.search.tryLabel.uppercaseFor(lang))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            strings.search.suggestions.forEach { suggestion ->
                Box(
                    Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, colors.borderStrong, RoundedCornerShape(20.dp))
                        .clickable { onRun(suggestion) }
                        .semantics { role = Role.Button }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) { Text(suggestion, style = LamplightType.bodyS().copy(fontWeight = FontWeight.Medium), color = colors.textPrimary) }
            }
        }
    }
}

@Composable
private fun TypeChips(state: SearchState, strings: AppStrings, onKind: (SearchKind?) -> Unit) {
    val s = strings.search
    fun a11y(label: String, n: Int) = s.chipCount.replace("{type}", label).replace("{n}", n.toString())
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchTypeChip(s.all, state.hits.size, state.selectedKind == null, a11y(s.all, state.hits.size), { onKind(null) })
        state.kindCounts.forEach { (kind, n) ->
            val label = kind.pluralLabel(strings)
            SearchTypeChip(label, n, state.selectedKind == kind, a11y(label, n), { onKind(kind) })
        }
    }
}

@Composable
private fun StatusLine(state: SearchState, strings: AppStrings) {
    val n = state.filtered.size
    val text = if (n == 1) strings.search.oneResult else strings.search.resultsCount.replace("{n}", n.toString())
    // Announced only after a 600ms typing pause, not on every keystroke.
    var announced by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        delay(600)
        announced = text
    }
    Box(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp)) {
        Text(text, style = LamplightType.meta(), color = LamplightTheme.colors.textTertiary)
        Box(Modifier.size(1.dp).semantics { liveRegion = LiveRegionMode.Polite; contentDescription = announced })
    }
}

@Composable
private fun ResultRow(hit: SearchHit, strings: AppStrings, lang: String, onClick: () -> Unit) {
    val item = hit.item
    SearchResultRow(
        typeLabel = (item.utilityCategory?.takeIf { item.pointCount == null }?.tileLabel(strings.essentials) ?: item.kind.singularLabel(strings)).uppercaseFor(lang),
        name = item.name,
        matchStart = hit.start,
        matchLength = hit.length,
        context = item.contextLine(strings, lang),
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
private fun IncompleteNotice(strings: AppStrings, onRetry: () -> Unit) {
    val colors = LamplightTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        EssentialsIcon(EssentialsGlyph.Warning, colors.lamp, 18.dp)
        Text(
            text = strings.search.incomplete,
            style = LamplightType.bodyS(),
            color = HomePalette.onGradientSecondary,
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
            Text(strings.search.retry, style = LamplightType.bodyS().copy(fontWeight = FontWeight.SemiBold), color = colors.lamp)
        }
    }
}

@Composable
private fun NoResults(query: String, strings: AppStrings, onBrowseExplore: () -> Unit) {
    val colors = LamplightTheme.colors
    val title = strings.search.nothingFor.replace("{q}", query)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchGlyphIcon(SearchGlyph.SearchMinus, colors.lamp, 44.dp)
        Text(
            text = title,
            style = LamplightType.sectionTitleS(),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite; heading() }
        )
        Text(
            text = strings.search.nothingBody,
            style = LamplightType.body().copy(fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal),
            color = HomePalette.onGradientSecondary,
            textAlign = TextAlign.Center
        )
        OutlinePill(strings.search.browseExplore, onBrowseExplore, Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun NotReady(strings: AppStrings, onRetry: () -> Unit) {
    val colors = LamplightTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.search.notReady,
            style = LamplightType.body().copy(fontSize = 15.sp, lineHeight = 23.sp),
            color = HomePalette.onGradientSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        OutlinePill(strings.search.retry, onRetry)
    }
}

@Composable
private fun OutlinePill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LamplightTheme.colors
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, colors.borderStrong, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, style = LamplightType.body().copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary) }
}

private fun SearchKind.pluralLabel(strings: AppStrings): String = when (this) {
    SearchKind.PLACE -> strings.search.places
    SearchKind.ACTIVITY -> strings.search.activities
    SearchKind.STORY -> strings.search.stories
    SearchKind.ESSENTIAL -> strings.search.essentials
    SearchKind.EVENT -> strings.search.events
    SearchKind.UTILITY -> strings.search.mapPoints
    SearchKind.SERVICE -> strings.search.services
    SearchKind.NOTICE -> strings.search.updates
}

private fun SearchKind.singularLabel(strings: AppStrings): String = when (this) {
    SearchKind.PLACE -> strings.search.place
    SearchKind.ACTIVITY -> strings.search.activity
    SearchKind.STORY -> strings.search.story
    SearchKind.ESSENTIAL -> strings.search.essential
    SearchKind.EVENT -> strings.search.event
    SearchKind.UTILITY -> strings.search.mapPoints
    SearchKind.SERVICE -> strings.search.service
    SearchKind.NOTICE -> strings.search.update
}

/** `{subtype} · {area}` for places/activities, the group for Essentials, "Read or listen" for stories. */
private fun SearchIndexItem.contextLine(strings: AppStrings, lang: String): String? = when (kind) {
    SearchKind.PLACE -> listOfNotNull(subtype ?: placeCategory?.localizedLabel(strings), area).joinToString(" · ")
    SearchKind.ACTIVITY -> listOfNotNull(subtype, area).joinToString(" · ")
    SearchKind.STORY -> strings.search.readOrListen
    SearchKind.ESSENTIAL -> EssentialGroupKind.entries.firstOrNull { it.category == essentialCategory }?.title(strings.essentials)
    SearchKind.EVENT -> listOfNotNull(eventStart?.toDateTag(lang), area).joinToString(" · ")
    SearchKind.UTILITY -> pointCount?.let { strings.search.onMap.replace("{n}", it.toString()) } ?: area
    SearchKind.SERVICE, SearchKind.NOTICE -> area
}?.takeIf { it.isNotBlank() }
