package llc.bokadev.kompass.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.toDateTag
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.FavoriteItemType
import llc.bokadev.kompass.domain.model.FavoriteKey
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.screens.home.components.ArrowRightIcon
import llc.bokadev.kompass.presentation.screens.home.components.CheckIcon
import llc.bokadev.kompass.presentation.screens.home.components.HomeEventCard
import llc.bokadev.kompass.presentation.screens.home.components.LamplightTopBar
import llc.bokadev.kompass.presentation.screens.home.components.MustSeeCard
import llc.bokadev.kompass.presentation.screens.home.components.NearbyCardUi
import llc.bokadev.kompass.presentation.screens.home.components.NearbyStackCarousel
import llc.bokadev.kompass.presentation.screens.home.components.NoticeItem
import llc.bokadev.kompass.presentation.screens.home.components.SectionHeader
import llc.bokadev.kompass.presentation.theme.HomePalette
import llc.bokadev.kompass.presentation.theme.pageGradientBackground
import llc.bokadev.kompass.presentation.theme.LamplightDimens
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import llc.bokadev.kompass.presentation.theme.helveticaBold24
import llc.bokadev.kompass.presentation.theme.helveticaBold44
import llc.bokadev.kompass.presentation.theme.helveticaMedium13
import llc.bokadev.kompass.presentation.theme.helveticaMedium16
import llc.bokadev.kompass.presentation.theme.helveticaRegular14
import kotlin.time.ExperimentalTime

/**
 * The whole screen is one continuous light-purple-to-ink gradient (see
 * [HomePalette.pageGradientStops]) — sections don't carry their own background blocks. Early
 * sections (masthead, area control, Nearby) land on the light part of the gradient and use
 * dark ink text; later sections (Must See, Notices, Events) land on the dark part and use
 * light surface-based text. Nearby's own white cards read fine against either.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeState,
    favoriteKeySet: Set<FavoriteKey>,
    onAction: (HomeAction) -> Unit
) {
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()
    val onPlaceClick: (String) -> Unit = { id ->
        val origin = if (state.mustSeePlaces.any { it.id == id }) "must_see" else "nearby"
        onAction(HomeAction.PlaceClicked(id, origin))
    }
    // The bookmark's rendered state decides the action, so a repeated tap event can't flip it back.
    val onPlaceFavoriteToggle: (String) -> Unit = { id ->
        onAction(HomeAction.SetFavorite(id, saved = FavoriteKey(FavoriteItemType.PLACE, id) !in favoriteKeySet))
    }
    // Purely presentational: which sheet is open, not business state — HomeState owns
    // showLocationPrompt instead, since that visibility depends on permission/preference reads.
    var showAreaSheet by remember { mutableStateOf(false) }

    Box {
    Column(
        modifier = Modifier.fillMaxSize()
            .pageGradientBackground()
            .verticalScroll(rememberScrollState())
    ) {
        LamplightTopBar(
            state = state,
            strings = strings,
            onAreaClick = { showAreaSheet = true },
            onLanguageClick = { onAction(HomeAction.OpenLanguagePicker) },
            onSearchClick = { onAction(HomeAction.OpenSearch) },
            onUseMyLocationClick = { onAction(HomeAction.SelectArea(HomeArea.DEVICE)) }
        )
        Spacer(Modifier.height(28.dp))

        HomeSectionFrame(state, HomeSection.NEARBY, state.effectiveArea.sectionTitle(strings), onAction) {
            NearbyYouSection(
                state = state, lang = lang, strings = strings,
                onPlaceClick = onPlaceClick,
                onListenClick = { id -> onAction(HomeAction.ListenClicked(id)) },
                favoriteKeySet = favoriteKeySet, onPlaceFavoriteToggle = onPlaceFavoriteToggle,
                // Manual areas get "See all" too; the list opens filtered to the same area.
                onSeeAll = { onAction(HomeAction.SeeAllNearby) },
                title = state.effectiveArea.sectionTitle(strings),
                emptyMessage = strings.redesign.emptyArea
            )
        }
        Spacer(Modifier.height(28.dp))

        // Spec A4: an empty Must See hides entirely (no title, no placeholder) and has no "See all" in v1.
        val hideMustSee = state.sections[HomeSection.HIGHLIGHTS] == SectionStatus.READY && state.mustSeePlaces.isEmpty()
        if (!hideMustSee) {
            HomeSectionFrame(state, HomeSection.HIGHLIGHTS, strings.redesign.landmarks, onAction, darkZone = true) {
                MustSeeSection(
                    places = state.mustSeePlaces, lang = lang, strings = strings,
                    onPlaceClick = onPlaceClick, favoriteKeySet = favoriteKeySet,
                    onPlaceFavoriteToggle = onPlaceFavoriteToggle,
                    title = strings.redesign.landmarks
                )
            }
            Spacer(Modifier.height(28.dp))
        }

        // Successfully loaded but empty sections are hidden; loading and failure (with Retry) still show.
        val hideNotices = state.sections[HomeSection.NOTICES] == SectionStatus.READY && state.infoCenterNotices.isEmpty()
        if (!hideNotices) {
            HomeSectionFrame(state, HomeSection.NOTICES, strings.recentNews, onAction, darkZone = true) {
                InfoCenterPreviewSection(
                    notices = state.infoCenterNotices, lang = lang, strings = strings,
                    onNoticeClick = { id -> onAction(HomeAction.NoticeClicked(id)) },
                    onSeeAll = { onAction(HomeAction.SeeAllInfoCenter) }, title = strings.recentNews
                )
            }
            Spacer(Modifier.height(28.dp))
        }

        val hideEvents = state.sections[HomeSection.EVENTS] == SectionStatus.READY && state.upcomingEvents.isEmpty()
        if (!hideEvents) {
            HomeSectionFrame(state, HomeSection.EVENTS, strings.upcomingEvents, onAction, darkZone = true) {
                UpcomingEventsSection(
                    events = state.upcomingEvents, lang = lang, strings = strings,
                    onEventClick = { id -> onAction(HomeAction.EventClicked(id)) },
                    onSeeAll = { onAction(HomeAction.SeeAllEvents) },
                    title = strings.upcomingEvents
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (state.showLocationPrompt) {
        AlertDialog(
            onDismissRequest = { onAction(HomeAction.DismissLocationPrompt) },
            title = { Text(strings.locationPromptTitle) },
            text = { Text(strings.locationPromptBody) },
            confirmButton = {
                TextButton(onClick = { onAction(HomeAction.RequestLocationPermission) }) {
                    Text(strings.continueLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(HomeAction.DismissLocationPrompt) }) {
                    Text(strings.notNow)
                }
            }
        )
    }

    if (showAreaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAreaSheet = false },
            containerColor = HomePalette.surface
        ) {
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = strings.redesign.chooseArea,
                    style = helveticaBold24().copy(fontSize = 22.sp, lineHeight = 28.sp),
                    color = HomePalette.ink,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HomeArea.entries.forEach { area ->
                    val selected = state.area == area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) HomePalette.selection else Color.Transparent)
                            .clickable {
                                showAreaSheet = false
                                onAction(HomeAction.SelectArea(area))
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = area.label(strings),
                            style = helveticaMedium16().copy(fontSize = 16.sp, lineHeight = 24.sp),
                            color = HomePalette.ink,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            CheckIcon(color = HomePalette.action, size = 16.dp)
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun homeHeadingStyle() = helveticaBold24().copy(fontSize = 22.sp, lineHeight = 28.sp)

@Composable
private fun homeBodyStyle() = helveticaMedium16().copy(fontSize = 16.sp, lineHeight = 24.sp)

@Composable
private fun homeMetaStyle() = helveticaRegular14().copy(fontSize = 14.sp, lineHeight = 20.sp)

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
    if (darkZone) HomePalette.ink.copy(alpha = 0.34f) else null

@Composable
private fun HomeSectionFrame(
    state: HomeState, section: HomeSection, title: String,
    onAction: (HomeAction) -> Unit, darkZone: Boolean = false, content: @Composable () -> Unit
) {
    val strings = rememberAppStrings()
    // All lettering on Home's gradient is white (owner's call, 2026-10-06).
    val titleColor = HomePalette.surface
    val metaColor = HomePalette.surface.copy(alpha = 0.7f)
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
                if (state.sections[section] == SectionStatus.FAILED) strings.redesign.sectionError else strings.loadingLabel,
                style = homeMetaStyle(),
                color = if (state.sections[section] == SectionStatus.FAILED) titleColor else metaColor
            )
            if (state.sections[section] == SectionStatus.FAILED) {
                // Same night-on-lamp pill as the other screens' Retry (white on periwinkle was 3.9:1).
                Text(
                    text = strings.retry,
                    style = helveticaMedium13(),
                    color = LamplightTheme.colors.night,
                    modifier = Modifier
                        .heightIn(min = LamplightDimens.minTouchTarget)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LamplightTheme.colors.lamp)
                        .clickable { onAction(HomeAction.RetrySection(section)) }
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
    val colors = LamplightTheme.colors
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
                style = LamplightType.sectionTitle(),
                color = HomePalette.surface,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            Text(
                text = strings.redesign.wholeBay.uppercaseFor(lang),
                style = LamplightType.label(),
                color = HomePalette.surface,
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
                    strings = strings.redesign,
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
                    text = strings.home.aboutDestination.replace("{name}", name),
                    style = homeBodyStyle().copy(fontWeight = FontWeight.SemiBold),
                    color = HomePalette.surface
                )
                ArrowRightIcon(color = HomePalette.surface, size = 14.dp)
            }
        }
        if (state.nearbyPlaces.isEmpty()) {
            Text(
                text = emptyMessage,
                style = homeMetaStyle(),
                color = HomePalette.onGradientSecondary,
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
                strings = strings.redesign,
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
    strings: AppStrings,
    onEventClick: (String) -> Unit,
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
            titleColor = HomePalette.surface,
            labelColor = HomePalette.surface
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
                    venueLine = event.localizedVenue(lang) + event.toRecurrenceSuffix(strings),
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
            titleColor = HomePalette.surface,
            labelColor = HomePalette.surface
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
