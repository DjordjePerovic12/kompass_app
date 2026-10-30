package llc.bokadev.kompass.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.continue_label
import kompass.composeapp.generated.resources.home_about_destination
import kompass.composeapp.generated.resources.loading_label
import kompass.composeapp.generated.resources.location_prompt_body
import kompass.composeapp.generated.resources.location_prompt_title
import kompass.composeapp.generated.resources.not_now
import kompass.composeapp.generated.resources.recent_news
import kompass.composeapp.generated.resources.redesign_choose_area
import kompass.composeapp.generated.resources.redesign_empty_area
import kompass.composeapp.generated.resources.redesign_event_recurring_weekly
import kompass.composeapp.generated.resources.redesign_landmarks
import kompass.composeapp.generated.resources.redesign_section_error
import kompass.composeapp.generated.resources.redesign_whole_bay
import kompass.composeapp.generated.resources.retry
import kompass.composeapp.generated.resources.upcoming_events
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.toDateTag
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.HomeArea
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.home.components.HomeEventCard
import llc.bokadev.kompass.presentation.home.components.LamplightTopBar
import llc.bokadev.kompass.presentation.home.components.MustSeeCard
import llc.bokadev.kompass.presentation.home.components.NearbyCardUi
import llc.bokadev.kompass.presentation.home.components.NearbyStackCarousel
import llc.bokadev.kompass.presentation.home.components.SectionHeader
import llc.bokadev.kompass.presentation.home.utils.toHomeEventMeta
import llc.bokadev.kompass.presentation.home.utils.toMustSeeAreaLabel
import llc.bokadev.kompass.presentation.home.utils.toNearbyMeta
import llc.bokadev.kompass.presentation.home.utils.toRecurrenceSuffix
import llc.bokadev.kompass.presentation.shared.NoticeItem
import llc.bokadev.kompass.presentation.shared.icons.ArrowRightIcon
import llc.bokadev.kompass.presentation.shared.icons.CheckIcon
import llc.bokadev.kompass.presentation.shared.toNoticeLabel
import llc.bokadev.kompass.presentation.shared.toNoticeLevel
import llc.bokadev.kompass.ui.theme.KompassDimens
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import llc.bokadev.kompass.ui.theme.pageGradientStops
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

/**
 * The whole screen is one continuous light-purple-to-ink gradient (see
 * [pageGradientStops]) — sections don't carry their own background blocks. Early
 * sections (masthead, area control, Nearby) land on the light part of the gradient and use
 * dark ink text; later sections (Must See, Notices, Events) land on the dark part and use
 * light surface-based text. Nearby's own white cards read fine against either.
 *
 * Area names, place type labels and notice labels still come from the AppStrings tables
 * (through [HomeArea], the card formatting helpers and [toNoticeLabel]) until those shared
 * helpers are migrated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    state: HomeState,
    favoriteKeySet: Set<FavoriteKey>,
    onPlaceClick: (id: String, origin: String) -> Unit,
    onFavoriteChange: (placeId: String, saved: Boolean) -> Unit,
    onListenClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onNoticeClick: (String) -> Unit,
    onAreaSelect: (HomeArea) -> Unit,
    onRetrySectionClick: (HomeSection) -> Unit,
    onSeeAllNearbyClick: () -> Unit,
    onSeeAllInfoCenterClick: () -> Unit,
    onSeeAllEventsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onLocationPromptContinueClick: () -> Unit,
    onLocationPromptDismiss: () -> Unit
) {
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val strings = appStringsFor(lang)
    val onPlaceOpen: (String) -> Unit = { id ->
        val origin = if (state.mustSeePlaces.any { it.id == id }) "must_see" else "nearby"
        onPlaceClick(id, origin)
    }
    // The bookmark's rendered state decides the action, so a repeated tap event can't flip it back.
    val onPlaceFavoriteToggle: (String) -> Unit = { id ->
        onFavoriteChange(id, FavoriteKey(FavoriteItemType.PLACE, id) !in favoriteKeySet)
    }
    // Purely presentational: which sheet is open, not business state — HomeState owns
    // showLocationPrompt instead, since that visibility depends on permission/preference reads.
    var showAreaSheet by remember { mutableStateOf(false) }
    val landmarksTitle = stringResource(Res.string.redesign_landmarks)
    val recentNewsTitle = stringResource(Res.string.recent_news)
    val upcomingEventsTitle = stringResource(Res.string.upcoming_events)

    Box(modifier) {
    Column(
        modifier = Modifier.fillMaxSize()
            .pageGradientBackground()
            .verticalScroll(rememberScrollState())
    ) {
        LamplightTopBar(
            state = state,
            onAreaClick = { showAreaSheet = true },
            onLanguageClick = onLanguageClick,
            onSearchClick = onSearchClick,
            onUseMyLocationClick = { onAreaSelect(HomeArea.DEVICE) }
        )
        Spacer(Modifier.height(28.dp))

        HomeSectionFrame(state, HomeSection.NEARBY, state.effectiveArea.sectionTitle(strings), onRetrySectionClick) {
            NearbyYouSection(
                state = state, lang = lang, strings = strings,
                onPlaceClick = onPlaceOpen,
                onListenClick = onListenClick,
                favoriteKeySet = favoriteKeySet, onPlaceFavoriteToggle = onPlaceFavoriteToggle,
                // Manual areas get "See all" too; the list opens filtered to the same area.
                onSeeAll = onSeeAllNearbyClick,
                title = state.effectiveArea.sectionTitle(strings),
                emptyMessage = stringResource(Res.string.redesign_empty_area)
            )
        }
        Spacer(Modifier.height(28.dp))

        // Spec A4: an empty Must See hides entirely (no title, no placeholder) and has no "See all" in v1.
        val hideMustSee = state.sections[HomeSection.HIGHLIGHTS] == SectionStatus.READY && state.mustSeePlaces.isEmpty()
        if (!hideMustSee) {
            HomeSectionFrame(state, HomeSection.HIGHLIGHTS, landmarksTitle, onRetrySectionClick, darkZone = true) {
                MustSeeSection(
                    places = state.mustSeePlaces, lang = lang, strings = strings,
                    onPlaceClick = onPlaceOpen, favoriteKeySet = favoriteKeySet,
                    onPlaceFavoriteToggle = onPlaceFavoriteToggle,
                    title = landmarksTitle
                )
            }
            Spacer(Modifier.height(28.dp))
        }

        // Successfully loaded but empty sections are hidden; loading and failure (with Retry) still show.
        val hideNotices = state.sections[HomeSection.NOTICES] == SectionStatus.READY && state.infoCenterNotices.isEmpty()
        if (!hideNotices) {
            HomeSectionFrame(state, HomeSection.NOTICES, recentNewsTitle, onRetrySectionClick, darkZone = true) {
                InfoCenterPreviewSection(
                    notices = state.infoCenterNotices, lang = lang, strings = strings,
                    onNoticeClick = onNoticeClick,
                    onSeeAll = onSeeAllInfoCenterClick, title = recentNewsTitle
                )
            }
            Spacer(Modifier.height(28.dp))
        }

        val hideEvents = state.sections[HomeSection.EVENTS] == SectionStatus.READY && state.upcomingEvents.isEmpty()
        if (!hideEvents) {
            HomeSectionFrame(state, HomeSection.EVENTS, upcomingEventsTitle, onRetrySectionClick, darkZone = true) {
                UpcomingEventsSection(
                    events = state.upcomingEvents, lang = lang,
                    onEventClick = onEventClick,
                    onSeeAll = onSeeAllEventsClick,
                    title = upcomingEventsTitle
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (state.showLocationPrompt) {
        AlertDialog(
            onDismissRequest = onLocationPromptDismiss,
            title = { Text(stringResource(Res.string.location_prompt_title)) },
            text = { Text(stringResource(Res.string.location_prompt_body)) },
            confirmButton = {
                TextButton(onClick = onLocationPromptContinueClick) {
                    Text(stringResource(Res.string.continue_label))
                }
            },
            dismissButton = {
                TextButton(onClick = onLocationPromptDismiss) {
                    Text(stringResource(Res.string.not_now))
                }
            }
        )
    }

    if (showAreaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAreaSheet = false },
            containerColor = KompassTheme.colors.white
        ) {
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.redesign_choose_area),
                    style = KompassTheme.typography.helveticaBold24.copy(fontSize = 22.sp, lineHeight = 28.sp),
                    color = KompassTheme.colors.inkBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HomeArea.entries.forEach { area ->
                    val selected = state.area == area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) KompassTheme.colors.lavender else Color.Transparent)
                            .clickable {
                                showAreaSheet = false
                                onAreaSelect(area)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = area.label(strings),
                            style = KompassTheme.typography.helveticaMedium16.copy(fontSize = 16.sp, lineHeight = 24.sp),
                            color = KompassTheme.colors.inkBlack,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            CheckIcon(color = KompassTheme.colors.periwinkle, size = 16.dp)
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun homeHeadingStyle() = KompassTheme.typography.helveticaBold24.copy(fontSize = 22.sp, lineHeight = 28.sp)

@Composable
private fun homeBodyStyle() = KompassTheme.typography.helveticaMedium16.copy(fontSize = 16.sp, lineHeight = 24.sp)

@Composable
private fun homeMetaStyle() = KompassTheme.typography.helveticaRegular14.copy(fontSize = 14.sp, lineHeight = 20.sp)

/**
 * [darkZone] sections render their heading/metadata on a local translucent ink panel rather
 * than bare text on the raw gradient. The gradient's color at any given scroll offset depends
 * on total page height, which shifts with how much content each section above it actually
 * rendered (loading/empty/failed states are far shorter than a loaded section) — so white text
 * assumed to land on the gradient's dark half can end up on its light half instead. A local
 * opaque-ish backing makes contrast correct regardless of where the section actually lands.
 */
@Composable
private fun localSectionScrim(darkZone: Boolean): Color? =
    if (darkZone) KompassTheme.colors.inkBlack.copy(alpha = 0.34f) else null

@Composable
private fun HomeSectionFrame(
    state: HomeState, section: HomeSection, title: String,
    onRetry: (HomeSection) -> Unit, darkZone: Boolean = false, content: @Composable () -> Unit
) {
    // All lettering on Home's gradient is white (owner's call, 2026-10-06).
    val titleColor = KompassTheme.colors.white
    val metaColor = KompassTheme.colors.white.copy(alpha = 0.7f)
    val scrim = localSectionScrim(darkZone)
    when (state.sections[section]) {
        SectionStatus.READY -> content()
        else -> Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .let {
                    if (scrim != null) it.clip(RoundedCornerShape(16.dp)).background(scrim).padding(16.dp)
                    else it
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = homeHeadingStyle(), color = titleColor)
            // Failure text is white like the rest of Home's lettering: the error red measured 1.5–3:1
            // on the gradient. The message and the Retry pill carry the meaning.
            Text(
                if (state.sections[section] == SectionStatus.FAILED) stringResource(Res.string.redesign_section_error)
                else stringResource(Res.string.loading_label),
                style = homeMetaStyle(),
                color = if (state.sections[section] == SectionStatus.FAILED) titleColor else metaColor
            )
            if (state.sections[section] == SectionStatus.FAILED) {
                // Same night-on-lamp pill as the other screens' Retry (white on periwinkle was 3.9:1).
                Text(
                    text = stringResource(Res.string.retry),
                    style = KompassTheme.typography.helveticaMedium13,
                    color = KompassTheme.colors.nightBlack,
                    modifier = Modifier
                        .heightIn(min = KompassDimens.minTouchTarget)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KompassTheme.colors.lampYellow)
                        .clickable { onRetry(section) }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }
    }
}

/**
 * Free-scrolling row of Landmarks (Must See data) cards (spec A3/A5), no auto-advance. An
 * [EqualHeightRow] rather than a `LazyRow`, so every card takes the tallest card's height and
 * the save buttons line up (the spec accepts a non-lazy row for this list size). The trade-off:
 * no snap-to-card fling, since Compose's snap providers target lazy layouts only.
 * Cards are 196dp (224dp at font scale 1.3+), smaller than the spec's 232dp at the owner's request.
 */
@Composable
private fun MustSeeSection(
    places: List<Place>,
    lang: String,
    strings: AppStrings,
    onPlaceClick: (String) -> Unit,
    favoriteKeySet: Set<FavoriteKey>,
    onPlaceFavoriteToggle: (String) -> Unit,
    title: String
) {
    val cardWidth = if (LocalDensity.current.fontScale >= 1.3f) 224.dp else 196.dp
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                style = KompassTheme.typography.gloockRegular32,
                color = KompassTheme.colors.white,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            Text(
                text = stringResource(Res.string.redesign_whole_bay).uppercaseFor(lang),
                style = KompassTheme.typography.geologicaSemiBold11,
                color = KompassTheme.colors.white,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        EqualHeightRow(
            spacing = 12.dp,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            places.forEach { place ->
                MustSeeCard(
                    name = place.localizedName(lang),
                    areaLabel = place.toMustSeeAreaLabel(strings)?.uppercaseFor(lang),
                    description = place.localizedDescription(lang).takeIf { it.isNotBlank() },
                    imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) },
                    isSaved = favoriteKeySet.contains(FavoriteKey(FavoriteItemType.PLACE, place.id)),
                    onToggleSave = { onPlaceFavoriteToggle(place.id) },
                    onClick = { onPlaceClick(place.id) },
                    modifier = Modifier.width(cardWidth)
                )
            }
        }
    }
}

@Composable
private fun NearbyYouSection(
    state: HomeState,
    lang: String,
    strings: AppStrings,
    onPlaceClick: (String) -> Unit,
    onListenClick: (String) -> Unit,
    favoriteKeySet: Set<FavoriteKey>,
    onPlaceFavoriteToggle: (String) -> Unit,
    onSeeAll: (() -> Unit)?,
    title: String,
    emptyMessage: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = title, onSeeAll = onSeeAll)
        // The destination's editorial introduction sits apart from the attractions, never as a card.
        state.areaOverview?.let { overview ->
            val name = overview.localizedName(lang)
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button) { onPlaceClick(overview.id) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.home_about_destination, name),
                    style = homeBodyStyle().copy(fontWeight = FontWeight.SemiBold),
                    color = KompassTheme.colors.white
                )
                ArrowRightIcon(color = KompassTheme.colors.white, size = 14.dp)
            }
        }
        if (state.nearbyPlaces.isEmpty()) {
            Text(
                text = emptyMessage,
                style = homeMetaStyle(),
                color = KompassTheme.colors.white85,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            val items = state.nearbyPlaces.map { nearby ->
                val place = nearby.place
                NearbyCardUi(
                    id = place.id,
                    title = place.localizedName(lang),
                    meta = nearby.toNearbyMeta(showDistance = state.isUsingCurrentLocation, strings = strings),
                    imageUrl = place.photos.firstOrNull()?.let { buildPhotoUrl(it) },
                    hasAudio = place.localizedAudioFile(lang) != null,
                    isSaved = favoriteKeySet.contains(FavoriteKey(FavoriteItemType.PLACE, place.id))
                )
            }
            NearbyStackCarousel(
                items = items,
                onOpen = onPlaceClick,
                onPrimary = { item -> if (item.hasAudio) onListenClick(item.id) else onPlaceClick(item.id) },
                onToggleSave = onPlaceFavoriteToggle
            )
        }
    }
}

@Composable
@OptIn(ExperimentalTime::class)
private fun UpcomingEventsSection(
    events: List<Event>,
    lang: String,
    onEventClick: (String) -> Unit,
    onSeeAll: () -> Unit,
    title: String
) {
    val weeklyLabel = stringResource(Res.string.redesign_event_recurring_weekly)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = title,
            onSeeAll = onSeeAll,
            titleColor = KompassTheme.colors.white,
            labelColor = KompassTheme.colors.white
        )
        EqualHeightRow(
            spacing = 12.dp,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            events.forEach { event ->
                HomeEventCard(
                    name = event.localizedName(lang),
                    venueLine = event.localizedVenue(lang) + event.toRecurrenceSuffix(weeklyLabel),
                    dateTag = event.startTime.toDateTag(lang),
                    startTime = event.toHomeEventMeta(),
                    imageUrl = event.photos.firstOrNull()?.let { buildPhotoUrl(it) },
                    onClick = { onEventClick(event.id) },
                    modifier = Modifier.width(168.dp).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun InfoCenterPreviewSection(
    notices: List<InfoNotice>,
    lang: String,
    strings: AppStrings,
    onNoticeClick: (String) -> Unit,
    onSeeAll: () -> Unit,
    title: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = title,
            onSeeAll = onSeeAll,
            titleColor = KompassTheme.colors.white,
            labelColor = KompassTheme.colors.white
        )
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            notices.forEach { notice ->
                NoticeItem(
                    level = notice.toNoticeLevel(),
                    label = notice.toNoticeLabel(lang, strings),
                    title = notice.localizedTitle(lang),
                    summary = notice.localizedShortDescription(lang),
                    onClick = { onNoticeClick(notice.id) }
                )
            }
        }
    }
}

/**
 * A horizontal row whose children all get the tallest child's height, so cards with shorter
 * text keep their bottom-pinned buttons aligned with their neighbours.
 *
 * Not `Row(Modifier.height(IntrinsicSize.Max))`: text intrinsics ignore `maxLines`, so that
 * reserves height for a full un-ellipsized description and leaves a gap above the buttons.
 * Instead, the children are measured once at their real wrapped height to find the tallest,
 * then composed again at that fixed height. That's fine for the short, non-lazy rows used here.
 */
@Composable
private fun EqualHeightRow(
    spacing: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    SubcomposeLayout(modifier) { constraints ->
        val probe = Constraints(maxWidth = constraints.maxWidth)
        val tallest = subcompose("probe", content).maxOfOrNull { it.measure(probe).height } ?: 0
        val placeables = subcompose("content", content).map {
            it.measure(Constraints(maxWidth = constraints.maxWidth, minHeight = tallest, maxHeight = tallest))
        }
        val gap = spacing.roundToPx()
        val width = placeables.sumOf { it.width } + gap * (placeables.size - 1).coerceAtLeast(0)
        layout(width, tallest) {
            var x = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x, 0)
                x += placeable.width + gap
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenContentPreview() {
    KompassTheme {
        HomeScreenContent(
            state = HomeState(
                area = HomeArea.OLD_TOWN,
                sections = HomeSection.entries.associateWith { SectionStatus.READY } + (HomeSection.HIGHLIGHTS to SectionStatus.FAILED),
                upcomingEvents = listOf(
                    Event(
                        id = "fest", name = mapOf("en" to "Kotor Summer Fest"), description = emptyMap(),
                        venue = mapOf("en" to "Arms Square"), latitude = 42.42, longitude = 18.77, category = "music",
                        startTime = "2026-10-08T18:00:00Z", endTime = "2026-10-08T20:00:00Z", price = null,
                        ticketUrl = null, isRecurring = true, photos = emptyList()
                    )
                ),
                infoCenterNotices = listOf(
                    InfoNotice(
                        id = "ferry", cityId = "kotor", title = mapOf("en" to "Ferry timetable changes"),
                        shortDescription = mapOf("en" to "Winter schedule starts on Monday."), priority = "important"
                    )
                )
            ),
            favoriteKeySet = emptySet(),
            onPlaceClick = { _, _ -> },
            onFavoriteChange = { _, _ -> },
            onListenClick = {},
            onEventClick = {},
            onNoticeClick = {},
            onAreaSelect = {},
            onRetrySectionClick = {},
            onSeeAllNearbyClick = {},
            onSeeAllInfoCenterClick = {},
            onSeeAllEventsClick = {},
            onSearchClick = {},
            onLanguageClick = {},
            onLocationPromptContinueClick = {},
            onLocationPromptDismiss = {}
        )
    }
}
