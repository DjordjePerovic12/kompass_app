package llc.bokadev.kompass.presentation.essentials

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.bottom_essentials
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.detail_collapsed
import kompass.composeapp.generated.resources.detail_expanded
import kompass.composeapp.generated.resources.detail_read_more
import kompass.composeapp.generated.resources.detail_show_less
import kompass.composeapp.generated.resources.essentials_call_number
import kompass.composeapp.generated.resources.essentials_closest
import kompass.composeapp.generated.resources.essentials_connect_once
import kompass.composeapp.generated.resources.essentials_couldnt_load
import kompass.composeapp.generated.resources.essentials_distances_current
import kompass.composeapp.generated.resources.essentials_do_this_early
import kompass.composeapp.generated.resources.essentials_emergency
import kompass.composeapp.generated.resources.essentials_essential_info
import kompass.composeapp.generated.resources.essentials_hide_reminder
import kompass.composeapp.generated.resources.essentials_how_it_works
import kompass.composeapp.generated.resources.essentials_location_off
import kompass.composeapp.generated.resources.essentials_map
import kompass.composeapp.generated.resources.essentials_map_preview_description
import kompass.composeapp.generated.resources.essentials_nearby_count
import kompass.composeapp.generated.resources.essentials_none_nearby
import kompass.composeapp.generated.resources.essentials_offline_note
import kompass.composeapp.generated.resources.essentials_open
import kompass.composeapp.generated.resources.essentials_open_map
import kompass.composeapp.generated.resources.essentials_subtitle
import kompass.composeapp.generated.resources.essentials_tile_all_on_map
import kompass.composeapp.generated.resources.essentials_turn_on
import kompass.composeapp.generated.resources.essentials_utilities
import kompass.composeapp.generated.resources.essentials_what_do_you_need
import kompass.composeapp.generated.resources.retry
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.presentation.essentials.components.DialEntry
import llc.bokadev.kompass.presentation.essentials.components.DoThisEarlyCard
import llc.bokadev.kompass.presentation.essentials.components.EmergencyCard
import llc.bokadev.kompass.presentation.essentials.components.EssentialModuleGroup
import llc.bokadev.kompass.presentation.essentials.components.ModuleAction
import llc.bokadev.kompass.presentation.essentials.components.ModuleItem
import llc.bokadev.kompass.presentation.essentials.components.ModuleLabels
import llc.bokadev.kompass.presentation.essentials.components.NearestRow
import llc.bokadev.kompass.presentation.essentials.components.TopicIndexGrid
import llc.bokadev.kompass.presentation.essentials.components.TopicTile
import llc.bokadev.kompass.presentation.essentials.components.UtilityCategoryGrid
import llc.bokadev.kompass.presentation.essentials.components.UtilityCompactRow
import llc.bokadev.kompass.presentation.essentials.components.UtilitySummaryCard
import llc.bokadev.kompass.presentation.essentials.components.UtilityTile
import llc.bokadev.kompass.presentation.essentials.utils.EmergencyNumber
import llc.bokadev.kompass.presentation.essentials.utils.EssentialGroup
import llc.bokadev.kompass.presentation.essentials.utils.GENERAL_EMERGENCY
import llc.bokadev.kompass.presentation.essentials.utils.SUMMARY_UTILITY_CATEGORIES
import llc.bokadev.kompass.presentation.essentials.utils.UtilityCategorySection
import llc.bokadev.kompass.presentation.essentials.utils.firstParagraph
import llc.bokadev.kompass.presentation.essentials.utils.labelRes
import llc.bokadev.kompass.presentation.essentials.utils.purposeRes
import llc.bokadev.kompass.presentation.shared.EssentialGroupKind
import llc.bokadev.kompass.presentation.shared.LamplightHeader
import llc.bokadev.kompass.presentation.shared.LamplightSegmentedControl
import llc.bokadev.kompass.presentation.shared.SegmentOption
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import llc.bokadev.kompass.presentation.shared.cardTitleRes
import llc.bokadev.kompass.presentation.shared.formatDistance
import llc.bokadev.kompass.presentation.shared.icons.ChevronLeftIcon
import llc.bokadev.kompass.presentation.shared.icons.EssentialsGlyph
import llc.bokadev.kompass.presentation.shared.icons.EssentialsIcon
import llc.bokadev.kompass.presentation.shared.icons.glyph
import llc.bokadev.kompass.presentation.shared.linkTitle
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import llc.bokadev.kompass.presentation.shared.tileLabelRes
import llc.bokadev.kompass.presentation.shared.titleRes
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource
import llc.bokadev.kompass.presentation.shared.StatusBarScrim

/**
 * Lamplight Essentials (Essentials spec): answer a practical question in under ten seconds. Two
 * views behind a segmented control in the header, each keeping its own scroll position.
 */
@Composable
fun EssentialsScreenContent(
    modifier: Modifier = Modifier,
    state: EssentialsState,
    offline: Boolean,
    onLanguageClick: () -> Unit,
    onTabSelect: (EssentialsTab) -> Unit,
    onBackClick: () -> Unit,
    onItemToggle: (String) -> Unit,
    onItemExpand: (String) -> Unit,
    onHideReminderClick: () -> Unit,
    onRetryClick: () -> Unit,
    /** null category = "All on map"; a non-null id pre-selects that pin. */
    onUtilityMapClick: (UtilityCategory?, String?) -> Unit,
    onDialClick: (String) -> Unit,
    onLocationClick: (title: String, latitude: Double, longitude: Double) -> Unit,
    onLinkClick: (String) -> Unit,
    onTurnOnLocationClick: () -> Unit
) {
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val colors = KompassTheme.colors
    val infoScroll = rememberScrollState()
    val utilityScroll = rememberScrollState()
    val info = state.selectedTab == EssentialsTab.INFO

    var containerTop by remember { mutableStateOf(0f) }
    val activeScroll = if (info) infoScroll else utilityScroll
    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pageGradientBackground()
                .onGloballyPositioned { containerTop = it.positionInRoot().y }
                .verticalScroll(if (info) infoScroll else utilityScroll)
        ) {
            LamplightHeader(
                searchPlaceholder = null,
                onLanguageClick = onLanguageClick,
                onSearchClick = null,
                footer = {
                    LamplightSegmentedControl(
                        options = listOf(
                            SegmentOption(stringResource(Res.string.essentials_essential_info)) { EssentialsIcon(EssentialsGlyph.Document, it, 18.dp) },
                            SegmentOption(stringResource(Res.string.essentials_utilities)) { EssentialsIcon(EssentialsGlyph.Pin, it, 18.dp) }
                        ),
                        selectedIndex = if (info) 0 else 1,
                        onSelect = { onTabSelect(if (it == 0) EssentialsTab.INFO else EssentialsTab.UTILITY) }
                    )
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (state.focusModuleId != null) {
                        Row(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.Button, onClick = onBackClick)
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ChevronLeftIcon(color = colors.nightBlack, size = 16.dp)
                            Text(
                                text = stringResource(Res.string.detail_back),
                                style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                color = colors.nightBlack
                            )
                        }
                    }
                    Text(
                        text = stringResource(Res.string.bottom_essentials),
                        style = KompassTheme.typography.gloockRegular40,
                        color = colors.nightBlack,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = stringResource(Res.string.essentials_subtitle),
                        style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        color = colors.nightBlack
                    )
                }
            }
            if (offline && info && !state.essentialsFailed) {
                Text(
                    text = stringResource(Res.string.essentials_offline_note),
                    style = KompassTheme.typography.geologicaRegular13,
                    color = KompassTheme.colors.white85,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp)
                )
            }
            if (info) {
                InfoView(
                    state = state,
                    lang = lang,
                    scroll = infoScroll,
                    focusModuleId = state.focusModuleId,
                    containerTop = { containerTop },
                    onItemToggle = onItemToggle,
                    onItemExpand = onItemExpand,
                    onHideReminderClick = onHideReminderClick,
                    onRetryClick = onRetryClick,
                    onDialClick = onDialClick,
                    onLocationClick = onLocationClick,
                    onLinkClick = onLinkClick
                )
            } else {
                UtilitiesView(
                    state = state,
                    onRetryClick = onRetryClick,
                    onUtilityMapClick = onUtilityMapClick,
                    onTurnOnLocationClick = onTurnOnLocationClick
                )
            }
            // The shell already insets content above the floating bar; this is the spec's breathing room.
            Spacer(Modifier.height(26.dp))
        }
        StatusBarScrim(visible = activeScroll.value > 0)
    }
}

@Composable
private fun InfoView(
    state: EssentialsState,
    lang: String,
    scroll: ScrollState,
    focusModuleId: String?,
    containerTop: () -> Float,
    onItemToggle: (String) -> Unit,
    onItemExpand: (String) -> Unit,
    onHideReminderClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDialClick: (String) -> Unit,
    onLocationClick: (title: String, latitude: Double, longitude: Double) -> Unit,
    onLinkClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val reduceMotion = rememberReduceMotion()
    val density = LocalDensity.current
    val singleColumn = density.fontScale >= 1.3f
    // Each group heading's position in the window, so a jump puts the heading at the top of the
    // screen (bringIntoView would stop as soon as the heading peeks in above the floating bar).
    val headingTops = remember { mutableStateMapOf<EssentialGroupKind, Float>() }
    val topGap = with(density) { (WindowInsets.statusBars.getTop(density) + 16.dp.roundToPx()).toFloat() }
    fun jumpTo(kind: EssentialGroupKind) {
        val top = headingTops[kind] ?: return
        scope.launch { scroll.animateScrollTo((scroll.value + top - containerTop() - topGap).toInt().coerceAtLeast(0)) }
    }

    // From Search: once the module's group is laid out, expand the module and bring its group up.
    var focused by rememberSaveable { mutableStateOf(false) }
    val focusGroup = focusModuleId?.let { id -> state.groups.firstOrNull { g -> g.modules.any { it.id == id } }?.kind }
    LaunchedEffect(focusGroup, headingTops[focusGroup]) {
        if (focused || focusGroup == null || focusModuleId == null || headingTops[focusGroup] == null) return@LaunchedEffect
        focused = true
        onItemExpand(focusModuleId)
        jumpTo(focusGroup)
    }

    val mapLabel = stringResource(Res.string.essentials_map)
    val openLabel = stringResource(Res.string.essentials_open)
    val moduleLabels = ModuleLabels(
        stringResource(Res.string.detail_read_more),
        stringResource(Res.string.detail_show_less),
        stringResource(Res.string.detail_expanded),
        stringResource(Res.string.detail_collapsed)
    )

    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Order for repeated daily use: compact reminder, task shortcuts, then emergency numbers.
        state.priority?.takeIf { !state.reminderHidden }?.let { priority ->
            DoThisEarlyCard(
                label = stringResource(Res.string.essentials_do_this_early),
                title = priority.localizedTitle(lang),
                summary = priority.localizedContent(lang).firstParagraph(),
                actionLabel = stringResource(Res.string.essentials_how_it_works),
                onAction = {
                    onItemExpand(priority.id)
                    jumpTo(EssentialGroupKind.REGISTRATION)
                },
                dismissLabel = stringResource(Res.string.essentials_hide_reminder),
                onDismiss = onHideReminderClick
            )
        }

        if (state.groups.isNotEmpty()) {
            SectionTitle(stringResource(Res.string.essentials_what_do_you_need), Modifier.padding(top = 6.dp))
            TopicIndexGrid(
                tiles = state.groups.map { group ->
                    TopicTile(stringResource(group.kind.titleRes()), group.kind.glyph()) { jumpTo(group.kind) }
                },
                singleColumn = singleColumn
            )
        }

        EmergencyCard(
            title = stringResource(Res.string.essentials_emergency),
            primary = DialEntry(GENERAL_EMERGENCY.number, GENERAL_EMERGENCY.label()),
            secondary = state.emergencyNumbers.map { DialEntry(it.number, it.label()) },
            callDescription = stringResource(Res.string.essentials_call_number, "{number}", "{service}"),
            onDial = onDialClick,
            modifier = Modifier.padding(top = 6.dp)
        )

        if (state.groups.isEmpty()) {
            if (state.essentialsFailed) ConnectOnceMessage(onRetryClick)
            return@Column
        }

        state.groups.forEach { group ->
            Column(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .onGloballyPositioned { headingTops[group.kind] = it.positionInRoot().y },
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SectionTitle(stringResource(group.kind.titleRes()))
                Text(text = stringResource(group.kind.purposeRes()), style = KompassTheme.typography.geologicaRegular14, color = KompassTheme.colors.pebbleGray)
            }
            EssentialModuleGroup(
                items = group.modules.map { it.toModuleItem(lang, mapLabel, openLabel, onLocationClick, onLinkClick) },
                expandedIds = state.expandedIds,
                labels = moduleLabels,
                reduceMotion = reduceMotion,
                onToggle = onItemToggle
            )
        }
    }
}

/** A known service's translated label, else the CMS label. */
@Composable
private fun EmergencyNumber.label(): String = service?.let { stringResource(it.labelRes()) } ?: rawLabel

private fun EssentialGroupKind.glyph(): EssentialsGlyph = when (this) {
    EssentialGroupKind.REGISTRATION -> EssentialsGlyph.Document
    EssentialGroupKind.GETTING_AROUND -> EssentialsGlyph.Bus
    EssentialGroupKind.PRACTICAL -> EssentialsGlyph.Wallet
    EssentialGroupKind.LOCAL_TIPS -> EssentialsGlyph.Lightbulb
}

/** One CMS entry as an accordion row: a Map action per exact location, an Open action per link. */
private fun CityEssential.toModuleItem(
    lang: String,
    mapLabel: String,
    openLabel: String,
    onLocationClick: (title: String, latitude: Double, longitude: Double) -> Unit,
    onLinkClick: (String) -> Unit
): ModuleItem {
    val title = localizedTitle(lang)
    val locationActions = resolvedLocations(lang).mapNotNull { location ->
        val lat = location.latitude ?: return@mapNotNull null
        val lng = location.longitude ?: return@mapNotNull null
        val label = location.title.ifBlank { title }
        ModuleAction(EssentialsGlyph.Pin, label, mapLabel) { onLocationClick(label, lat, lng) }
    }
    val linkActions = links.filter { it.isNotBlank() }.map { url ->
        ModuleAction(EssentialsGlyph.ArrowUpRight, url.linkTitle(), openLabel) { onLinkClick(url) }
    }
    // No CMS summary field exists yet, so the subtitle stays hidden rather than showing link counts.
    return ModuleItem(id, title, subtitle = null, body = localizedContent(lang).trim(), actions = locationActions + linkActions)
}

@Composable
private fun UtilitiesView(
    state: EssentialsState,
    onRetryClick: () -> Unit,
    onUtilityMapClick: (UtilityCategory?, String?) -> Unit,
    onTurnOnLocationClick: () -> Unit
) {
    val location = state.currentLocation
    val fontScale = LocalDensity.current.fontScale
    val columns = when {
        fontScale >= 1.6f -> 2
        fontScale >= 1.3f -> 3
        else -> 4
    }
    val byCategory = state.sections.associateBy { it.category }

    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LocationStatusLine(locationKnown = location != null, onTurnOn = onTurnOnLocationClick)

        if (state.utilitiesFailed && state.sections.isEmpty()) {
            Text(
                text = stringResource(Res.string.essentials_couldnt_load, stringResource(Res.string.essentials_utilities)),
                style = KompassTheme.typography.geologicaRegular14,
                color = KompassTheme.colors.white85
            )
            RetryPill(stringResource(Res.string.retry), onRetryClick)
            return@Column
        }
        if (state.sections.isEmpty()) return@Column

        // Only categories with records get a tile, so no shortcut leads to an empty map.
        val tileOrder = listOf(UtilityCategory.EMERGENCY) + UtilityCategory.entries.filter { it != UtilityCategory.EMERGENCY }
        UtilityCategoryGrid(
            tiles = tileOrder.filter { it in byCategory }.map { category ->
                UtilityTile(stringResource(category.tileLabelRes()), category.glyph(), emergency = category == UtilityCategory.EMERGENCY) {
                    onUtilityMapClick(category, null)
                }
            } + UtilityTile(stringResource(Res.string.essentials_tile_all_on_map), EssentialsGlyph.Map) { onUtilityMapClick(null, null) },
            columns = columns
        )

        val summaries = SUMMARY_UTILITY_CATEGORIES.mapNotNull(byCategory::get)
        summaries.forEachIndexed { index, section ->
            val title = stringResource(section.category.cardTitleRes())
            UtilitySummaryCard(
                title = title,
                nearbyLabel = section.nearbyText(),
                closestLabel = stringResource(Res.string.essentials_closest),
                closestDistance = location?.let { section.closestDistanceMeters?.formatDistance() },
                pins = section.previewUtilities.mapNotNull { u ->
                    val lat = u.latitude ?: return@mapNotNull null
                    val lng = u.longitude ?: return@mapNotNull null
                    UtilityMapPin(u.id, u.name, GeoPoint(lat, lng))
                },
                userLocation = location,
                mapDescription = stringResource(Res.string.essentials_map_preview_description, section.previewUtilities.size.toString(), title),
                nearest = section.nearest.map { item ->
                    NearestRow(item.utility.id, item.utility.name, location?.let { item.distanceMeters?.formatDistance() })
                },
                openMapLabel = stringResource(Res.string.essentials_open_map),
                primary = index == 0,
                onOpenMap = { onUtilityMapClick(section.category, null) },
                onOpenItem = { id -> onUtilityMapClick(section.category, id) },
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        state.sections
            .filter { it.category !in SUMMARY_UTILITY_CATEGORIES }
            .forEach { section ->
                UtilityCompactRow(
                    title = stringResource(section.category.cardTitleRes()),
                    subtitle = section.nearbyText(),
                    glyph = section.category.glyph(),
                    closestDistance = location?.takeIf { section.nearbyCount > 0 }?.let { section.closestDistanceMeters?.formatDistance() },
                    onClick = { onUtilityMapClick(section.category, null) }
                )
            }
    }
}

@Composable
private fun UtilityCategorySection.nearbyText(): String =
    if (nearbyCount > 0) stringResource(Res.string.essentials_nearby_count, nearbyCount.toString())
    else stringResource(Res.string.essentials_none_nearby)

@Composable
private fun LocationStatusLine(locationKnown: Boolean, onTurnOn: () -> Unit) {
    val colors = KompassTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(colors.lampYellow.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(colors.chalkWhite))
        }
        if (locationKnown) {
            Text(text = stringResource(Res.string.essentials_distances_current), style = KompassTheme.typography.geologicaRegular14, color = KompassTheme.colors.white85)
        } else {
            Column {
                Text(text = stringResource(Res.string.essentials_location_off), style = KompassTheme.typography.geologicaRegular14, color = KompassTheme.colors.white85)
                Text(
                    text = stringResource(Res.string.essentials_turn_on),
                    style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline),
                    color = colors.lampYellow,
                    modifier = Modifier.heightIn(min = 44.dp).clickable(onClick = onTurnOn).padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = KompassTheme.typography.gloockRegular26,
        color = KompassTheme.colors.boneWhite,
        modifier = modifier.semantics { heading() }
    )
}

@Composable
private fun ConnectOnceMessage(onRetry: () -> Unit) {
    Column(Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = stringResource(Res.string.essentials_connect_once), style = KompassTheme.typography.geologicaMedium16, color = KompassTheme.colors.stoneGray)
        RetryPill(stringResource(Res.string.retry), onRetry)
    }
}

@Composable
private fun RetryPill(label: String, onClick: () -> Unit) {
    val colors = KompassTheme.colors
    Text(
        text = label,
        style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.SemiBold),
        color = colors.nightBlack,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(KompassShapes.pill)
            .background(colors.lampYellow)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 13.dp)
    )
}

@Preview
@Composable
private fun EssentialsScreenContentPreview() {
    val registration = CityEssential(
        id = "registration",
        key = "registration",
        category = EssentialCategory.CUSTOMS,
        title = mapOf("en" to "Tourist registration & tax"),
        content = mapOf("en" to "If your stay requires registration, complete it within the first 24 hours.")
    )
    KompassTheme {
        EssentialsScreenContent(
            state = EssentialsState(
                isLoading = false,
                priority = registration,
                groups = listOf(EssentialGroup(EssentialGroupKind.REGISTRATION, listOf(registration)))
            ),
            offline = false,
            onLanguageClick = {},
            onTabSelect = {},
            onBackClick = {},
            onItemToggle = {},
            onItemExpand = {},
            onHideReminderClick = {},
            onRetryClick = {},
            onUtilityMapClick = { _, _ -> },
            onDialClick = {},
            onLocationClick = { _, _, _ -> },
            onLinkClick = {},
            onTurnOnLocationClick = {}
        )
    }
}
