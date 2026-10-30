package llc.bokadev.kompass.presentation.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.activities_title
import kompass.composeapp.generated.resources.events_subtitle
import kompass.composeapp.generated.resources.events_title
import kompass.composeapp.generated.resources.explore_further_out
import kompass.composeapp.generated.resources.explore_important_notice_active
import kompass.composeapp.generated.resources.explore_next_event
import kompass.composeapp.generated.resources.explore_read
import kompass.composeapp.generated.resources.explore_read_listen
import kompass.composeapp.generated.resources.explore_stories_subtitle
import kompass.composeapp.generated.resources.explore_whats_on
import kompass.composeapp.generated.resources.info_center_subtitle
import kompass.composeapp.generated.resources.info_center_title
import kompass.composeapp.generated.resources.redesign_activities_subtitle
import kompass.composeapp.generated.resources.redesign_explore
import kompass.composeapp.generated.resources.redesign_explore_subtitle
import kompass.composeapp.generated.resources.redesign_food
import kompass.composeapp.generated.resources.redesign_food_subtitle
import kompass.composeapp.generated.resources.redesign_local_stories
import kompass.composeapp.generated.resources.redesign_places
import kompass.composeapp.generated.resources.redesign_places_subtitle
import kompass.composeapp.generated.resources.redesign_worth_drive
import kompass.composeapp.generated.resources.redesign_worth_drive_subtitle
import kompass.composeapp.generated.resources.search_placeholder
import kompass.composeapp.generated.resources.services_subtitle
import kompass.composeapp.generated.resources.services_title
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.toDayOfMonth
import llc.bokadev.kompass.core.utils.toMonthAbbrev
import llc.bokadev.kompass.presentation.explore.components.ExploreGlyph
import llc.bokadev.kompass.presentation.explore.components.ExploreGroupedList
import llc.bokadev.kompass.presentation.explore.components.ExploreHeroCard
import llc.bokadev.kompass.presentation.explore.components.ExploreListRow
import llc.bokadev.kompass.presentation.explore.components.ExploreRowLeading
import llc.bokadev.kompass.presentation.explore.components.ExploreStoryCard
import llc.bokadev.kompass.presentation.explore.components.ExploreTileCard
import llc.bokadev.kompass.presentation.explore.components.ExploreTileStyle
import llc.bokadev.kompass.presentation.explore.components.ExploreWideArchCard
import llc.bokadev.kompass.presentation.shared.LamplightHeader
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.Box
import llc.bokadev.kompass.presentation.shared.StatusBarScrim

/**
 * Lamplight Explore (Explore spec): a grouped discovery hub where each group has a deliberately
 * different shape (hero photo, two contrasting tiles, a wide arch card, a yellow story card, and a
 * grouped list), not a grid of equal category tiles.
 */
@Composable
fun ExploreScreenContent(
    modifier: Modifier = Modifier,
    state: ExploreState,
    onDestinationClick: (ExploreDestination) -> Unit,
    onSearchClick: () -> Unit,
    onLanguageClick: () -> Unit
) {
    fun open(destination: ExploreDestination): () -> Unit = { onDestinationClick(destination) }
    val colors = KompassTheme.colors
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val stackTiles = LocalDensity.current.fontScale >= 1.3f

    val scroll = rememberScrollState()
    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pageGradientBackground()
                .verticalScroll(scroll)
        ) {
            LamplightHeader(
                searchPlaceholder = stringResource(Res.string.search_placeholder),
                onLanguageClick = onLanguageClick,
                onSearchClick = onSearchClick
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(Res.string.redesign_explore),
                        style = KompassTheme.typography.gloockRegular40,
                        color = colors.nightBlack,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = stringResource(Res.string.redesign_explore_subtitle),
                        style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        color = colors.nightBlack
                    )
                }
            }

            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExploreHeroCard(
                    title = stringResource(Res.string.redesign_places),
                    subtitle = stringResource(Res.string.redesign_places_subtitle),
                    imageUrl = state.heroPhoto?.let(::buildPhotoUrl),
                    onClick = open(ExploreDestination.PLACES)
                )
                DiscoveryTiles(stackTiles, ::open)
                if (state.hasWorthTheDrive) {
                    ExploreWideArchCard(
                        label = stringResource(Res.string.explore_further_out),
                        title = stringResource(Res.string.redesign_worth_drive),
                        subtitle = stringResource(Res.string.redesign_worth_drive_subtitle),
                        imageUrl = state.worthTheDrivePhoto?.let(::buildPhotoUrl),
                        onClick = open(ExploreDestination.WORTH_THE_DRIVE)
                    )
                }
            }

            if (state.hasStories) {
                ExploreStoryCard(
                    label = stringResource(if (state.storiesHaveAudio) Res.string.explore_read_listen else Res.string.explore_read),
                    title = stringResource(Res.string.redesign_local_stories),
                    subtitle = stringResource(Res.string.explore_stories_subtitle),
                    onClick = open(ExploreDestination.STORIES),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 30.dp)
                )
            }

            Text(
                text = stringResource(Res.string.explore_whats_on),
                style = KompassTheme.typography.gloockRegular26,
                color = colors.boneWhite,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 12.dp)
                    .semantics { heading() }
            )
            ExploreGroupedList(
                rows = whatsOnRows(state, lang, ::open),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            // The shell already insets content above the floating bar; this is the spec's breathing room.
            Spacer(Modifier.height(26.dp))
        }
        StatusBarScrim(visible = scroll.value > 0)
    }
}

@Composable
private fun DiscoveryTiles(stacked: Boolean, open: (ExploreDestination) -> () -> Unit) {
    val foodTitle = stringResource(Res.string.redesign_food)
    val foodSubtitle = stringResource(Res.string.redesign_food_subtitle)
    val activitiesTitle = stringResource(Res.string.activities_title)
    val activitiesSubtitle = stringResource(Res.string.redesign_activities_subtitle)
    val eat: @Composable (Modifier) -> Unit = { modifier ->
        ExploreTileCard(foodTitle, foodSubtitle, ExploreGlyph.ForkKnife, ExploreTileStyle.Limestone, open(ExploreDestination.EAT_AND_DRINK), modifier)
    }
    val activities: @Composable (Modifier) -> Unit = { modifier ->
        ExploreTileCard(activitiesTitle, activitiesSubtitle, ExploreGlyph.Walker, ExploreTileStyle.Raised, open(ExploreDestination.ACTIVITIES), modifier)
    }
    if (stacked) {
        // Large font scales: side-by-side titles would wrap past two lines, so stack instead.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            eat(Modifier.fillMaxWidth())
            activities(Modifier.fillMaxWidth())
        }
    } else {
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            eat(Modifier.weight(1f).fillMaxHeight())
            activities(Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun whatsOnRows(state: ExploreState, lang: String, open: (ExploreDestination) -> () -> Unit): List<ExploreListRow> {
    val start = state.nextEventStart
    val day = start?.toDayOfMonth()?.takeIf { it != "--" }
    val eventsLeading = if (start != null && day != null) {
        ExploreRowLeading.DateTile(month = start.toMonthAbbrev(lang), day = day)
    } else {
        ExploreRowLeading.Icon(ExploreGlyph.Calendar)
    }
    val nextEventTemplate = stringResource(Res.string.explore_next_event, "{date}")
    val eventsExtra = (eventsLeading as? ExploreRowLeading.DateTile)
        ?.let { nextEventTemplate.replace("{date}", "${it.day} ${it.month}") }
    val importantNotice = stringResource(Res.string.explore_important_notice_active)
    return listOf(
        ExploreListRow(stringResource(Res.string.events_title), stringResource(Res.string.events_subtitle), eventsLeading, eventsExtra, open(ExploreDestination.EVENTS)),
        ExploreListRow(
            stringResource(Res.string.info_center_title),
            stringResource(Res.string.info_center_subtitle),
            ExploreRowLeading.Icon(ExploreGlyph.Info, alertDot = state.hasImportantNotice),
            importantNotice.takeIf { state.hasImportantNotice },
            open(ExploreDestination.LOCAL_UPDATES)
        ),
        ExploreListRow(stringResource(Res.string.services_title), stringResource(Res.string.services_subtitle), ExploreRowLeading.Icon(ExploreGlyph.Wrench), null, open(ExploreDestination.SERVICES))
    )
}

@Preview
@Composable
private fun ExploreScreenContentPreview() {
    KompassTheme {
        ExploreScreenContent(
            state = ExploreState(storiesHaveAudio = true, nextEventStart = "2099-10-12T18:00:00Z", hasImportantNotice = true),
            onDestinationClick = {},
            onSearchClick = {},
            onLanguageClick = {}
        )
    }
}
