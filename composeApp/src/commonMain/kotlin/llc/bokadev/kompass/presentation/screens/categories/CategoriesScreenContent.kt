package llc.bokadev.kompass.presentation.screens.categories

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.toDayOfMonth
import llc.bokadev.kompass.core.utils.toMonthAbbrev
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreGlyph
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreGroupedList
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreHeroCard
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreListRow
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreRowLeading
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreStoryCard
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreTileCard
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreTileStyle
import llc.bokadev.kompass.presentation.screens.categories.components.ExploreWideArchCard
import llc.bokadev.kompass.presentation.shared.LamplightHeader
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import llc.bokadev.kompass.presentation.theme.pageGradientBackground

/**
 * Lamplight Explore (Explore spec): a grouped discovery hub where each group has a deliberately
 * different shape (hero photo, two contrasting tiles, a wide arch card, a yellow story card, and a
 * grouped list), not a grid of equal category tiles.
 */
@Composable
fun CategoriesScreenContent(
    state: CategoriesState,
    strings: AppStrings,
    onAction: (CategoriesAction) -> Unit
) {
    fun open(destination: ExploreDestination): () -> Unit = { onAction(CategoriesAction.Open(destination)) }
    val colors = LamplightTheme.colors
    val lang = currentAppLanguage()
    val copy = strings.redesign
    val stackTiles = LocalDensity.current.fontScale >= 1.3f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pageGradientBackground()
            .verticalScroll(rememberScrollState())
    ) {
        LamplightHeader(
            searchPlaceholder = strings.searchPlaceholder,
            onLanguageClick = { onAction(CategoriesAction.OpenLanguage) },
            onSearchClick = { onAction(CategoriesAction.OpenSearch) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = copy.explore,
                    style = LamplightType.areaTitle(),
                    color = colors.night,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = copy.exploreSubtitle,
                    style = LamplightType.body().copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    color = colors.night
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExploreHeroCard(
                title = copy.places,
                subtitle = copy.placesSubtitle,
                imageUrl = state.heroPhoto?.let(::buildPhotoUrl),
                onClick = open(ExploreDestination.PLACES)
            )
            DiscoveryTiles(strings, stackTiles, ::open)
            if (state.hasWorthTheDrive) {
                ExploreWideArchCard(
                    label = strings.explore.furtherOut,
                    title = copy.worthDrive,
                    subtitle = copy.worthDriveSubtitle,
                    imageUrl = state.worthTheDrivePhoto?.let(::buildPhotoUrl),
                    onClick = open(ExploreDestination.WORTH_THE_DRIVE)
                )
            }
        }

        if (state.hasStories) {
            ExploreStoryCard(
                label = if (state.storiesHaveAudio) strings.explore.readListen else strings.explore.read,
                title = copy.localStories,
                subtitle = strings.explore.storiesSubtitle,
                onClick = open(ExploreDestination.STORIES),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 30.dp)
            )
        }

        Text(
            text = strings.explore.whatsOn,
            style = LamplightType.sectionTitleS(),
            color = colors.textPrimary,
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 12.dp)
                .semantics { heading() }
        )
        ExploreGroupedList(
            rows = whatsOnRows(state, strings, lang, ::open),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        // The shell already insets content above the floating bar; this is the spec's breathing room.
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun DiscoveryTiles(strings: AppStrings, stacked: Boolean, open: (ExploreDestination) -> () -> Unit) {
    val eat: @Composable (Modifier) -> Unit = { modifier ->
        ExploreTileCard(strings.redesign.food, strings.redesign.foodSubtitle, ExploreGlyph.ForkKnife, ExploreTileStyle.Limestone, open(ExploreDestination.EAT_AND_DRINK), modifier)
    }
    val activities: @Composable (Modifier) -> Unit = { modifier ->
        ExploreTileCard(strings.activitiesTitle, strings.redesign.activitiesSubtitle, ExploreGlyph.Walker, ExploreTileStyle.Raised, open(ExploreDestination.ACTIVITIES), modifier)
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

private fun whatsOnRows(state: CategoriesState, strings: AppStrings, lang: String, open: (ExploreDestination) -> () -> Unit): List<ExploreListRow> {
    val start = state.nextEventStart
    val day = start?.toDayOfMonth()?.takeIf { it != "--" }
    val eventsLeading = if (start != null && day != null) {
        ExploreRowLeading.DateTile(month = start.toMonthAbbrev(lang), day = day)
    } else {
        ExploreRowLeading.Icon(ExploreGlyph.Calendar)
    }
    val eventsExtra = (eventsLeading as? ExploreRowLeading.DateTile)
        ?.let { strings.explore.nextEvent.replace("{date}", "${it.day} ${it.month}") }
    return listOf(
        ExploreListRow(strings.eventsTitle, strings.eventsSubtitle, eventsLeading, eventsExtra, open(ExploreDestination.EVENTS)),
        ExploreListRow(
            strings.infoCenterTitle,
            strings.infoCenterSubtitle,
            ExploreRowLeading.Icon(ExploreGlyph.Info, alertDot = state.hasImportantNotice),
            strings.explore.importantNoticeActive.takeIf { state.hasImportantNotice },
            open(ExploreDestination.LOCAL_UPDATES)
        ),
        ExploreListRow(strings.servicesTitle, strings.servicesSubtitle, ExploreRowLeading.Icon(ExploreGlyph.Wrench), null, open(ExploreDestination.SERVICES))
    )
}
