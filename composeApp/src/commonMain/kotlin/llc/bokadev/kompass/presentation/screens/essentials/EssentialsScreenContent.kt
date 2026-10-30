package llc.bokadev.kompass.presentation.screens.essentials

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.Role
import llc.bokadev.kompass.presentation.screens.home.components.ChevronLeftIcon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.util.AppStrings
import llc.bokadev.kompass.core.util.currentAppLanguage
import llc.bokadev.kompass.core.util.rememberAppStrings
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.presentation.screens.essentials.components.DialEntry
import llc.bokadev.kompass.presentation.screens.essentials.components.DoThisEarlyCard
import llc.bokadev.kompass.presentation.screens.essentials.components.EmergencyCard
import llc.bokadev.kompass.presentation.screens.essentials.components.EssentialModuleGroup
import llc.bokadev.kompass.presentation.screens.essentials.components.EssentialsGlyph
import llc.bokadev.kompass.presentation.screens.essentials.components.EssentialsIcon
import llc.bokadev.kompass.presentation.screens.essentials.components.ModuleAction
import llc.bokadev.kompass.presentation.screens.essentials.components.ModuleItem
import llc.bokadev.kompass.presentation.screens.essentials.components.ModuleLabels
import llc.bokadev.kompass.presentation.screens.essentials.components.NearestRow
import llc.bokadev.kompass.presentation.screens.essentials.components.TopicIndexGrid
import llc.bokadev.kompass.presentation.screens.essentials.components.TopicTile
import llc.bokadev.kompass.presentation.screens.essentials.components.UtilityCategoryGrid
import llc.bokadev.kompass.presentation.screens.essentials.components.UtilityCompactRow
import llc.bokadev.kompass.presentation.screens.essentials.components.UtilitySummaryCard
import llc.bokadev.kompass.presentation.screens.essentials.components.UtilityTile
import llc.bokadev.kompass.presentation.screens.essentials.components.glyph
import llc.bokadev.kompass.presentation.shared.LamplightHeader
import llc.bokadev.kompass.presentation.shared.LamplightSegmentedControl
import llc.bokadev.kompass.presentation.shared.SegmentOption
import llc.bokadev.kompass.presentation.shared.UtilityMapPin
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import llc.bokadev.kompass.presentation.theme.HomePalette
import llc.bokadev.kompass.presentation.theme.LamplightShapes
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType
import llc.bokadev.kompass.presentation.theme.pageGradientBackground

/** Everything Essentials hands off: dialer, exact-coordinate maps, links, the utility map, settings. */
/**
 * Lamplight Essentials (Essentials spec): answer a practical question in under ten seconds. Two
 * views behind a segmented control in the header, each keeping its own scroll position.
 */
@Composable
fun EssentialsScreenContent(
    state: EssentialsState,
    offline: Boolean,
    onAction: (EssentialsAction) -> Unit
) {
    val strings = rememberAppStrings()
    val colors = LamplightTheme.colors
    val infoScroll = rememberScrollState()
    val utilityScroll = rememberScrollState()
    val info = state.selectedTab == EssentialsTab.INFO

    var containerTop by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pageGradientBackground()
            .onGloballyPositioned { containerTop = it.positionInRoot().y }
            .verticalScroll(if (info) infoScroll else utilityScroll)
    ) {
        LamplightHeader(
            searchPlaceholder = null,
            onLanguageClick = { onAction(EssentialsAction.OpenLanguage) },
            onSearchClick = null,
            footer = {
                LamplightSegmentedControl(
                    options = listOf(
                        SegmentOption(strings.essentials.essentialInfo) { EssentialsIcon(EssentialsGlyph.Document, it, 18.dp) },
                        SegmentOption(strings.essentials.utilities) { EssentialsIcon(EssentialsGlyph.Pin, it, 18.dp) }
                    ),
                    selectedIndex = if (info) 0 else 1,
                    onSelect = { onAction(EssentialsAction.SelectTab(if (it == 0) EssentialsTab.INFO else EssentialsTab.UTILITY)) }
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (state.focusModuleId != null) {
                    val back = { onAction(EssentialsAction.Back) }
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(role = Role.Button, onClick = back)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChevronLeftIcon(color = colors.night, size = 16.dp)
                        Text(
                            text = strings.detail.back,
                            style = LamplightType.body().copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            color = colors.night
                        )
                    }
                }
                Text(
                    text = strings.bottomEssentials,
                    style = LamplightType.areaTitle(),
                    color = colors.night,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = strings.essentials.subtitle,
                    style = LamplightType.body().copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    color = colors.night
                )
            }
        }
        if (offline && info && !state.essentialsFailed) {
            Text(
                text = strings.essentials.offlineNote,
                style = LamplightType.meta(),
                color = HomePalette.onGradientSecondary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp)
            )
        }
        if (info) {
            InfoView(state, strings, onAction, infoScroll, state.focusModuleId) { containerTop }
        } else {
            UtilitiesView(state, strings, onAction)
        }
        // The shell already insets content above the floating bar; this is the spec's breathing room.
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun InfoView(
    state: EssentialsState,
    strings: AppStrings,
    onAction: (EssentialsAction) -> Unit,
    scroll: ScrollState,
    focusModuleId: String?,
    containerTop: () -> Float
) {
    val s = strings.essentials
    val lang = currentAppLanguage()
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
        onAction(EssentialsAction.ExpandItem(focusModuleId))
        jumpTo(focusGroup)
    }

    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Order for repeated daily use: compact reminder, task shortcuts, then emergency numbers.
        state.priority?.takeIf { !state.reminderHidden }?.let { priority ->
            DoThisEarlyCard(
                label = s.doThisEarly,
                title = priority.localizedTitle(lang),
                summary = priority.localizedContent(lang).firstParagraph(),
                actionLabel = s.howItWorks,
                onAction = {
                    onAction(EssentialsAction.ExpandItem(priority.id))
                    jumpTo(EssentialGroupKind.REGISTRATION)
                },
                dismissLabel = s.hideReminder,
                onDismiss = { onAction(EssentialsAction.HideReminder) }
            )
        }

        if (state.groups.isNotEmpty()) {
            SectionTitle(s.whatDoYouNeed, Modifier.padding(top = 6.dp))
            TopicIndexGrid(
                tiles = state.groups.map { group ->
                    TopicTile(group.kind.title(s), group.kind.glyph()) { jumpTo(group.kind) }
                },
                singleColumn = singleColumn
            )
        }

        EmergencyCard(
            title = s.emergency,
            primary = DialEntry(GENERAL_EMERGENCY.number, GENERAL_EMERGENCY.label(s)),
            secondary = state.emergencyNumbers.map { DialEntry(it.number, it.label(s)) },
            callDescription = s.callNumber,
            onDial = { onAction(EssentialsAction.Dial(it)) },
            modifier = Modifier.padding(top = 6.dp)
        )

        if (state.groups.isEmpty()) {
            if (state.essentialsFailed) ConnectOnceMessage(strings) { onAction(EssentialsAction.Retry) }
            return@Column
        }

        state.groups.forEach { group ->
            Column(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .onGloballyPositioned { headingTops[group.kind] = it.positionInRoot().y },
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SectionTitle(group.kind.title(s))
                Text(text = group.kind.purpose(s), style = LamplightType.bodyS(), color = LamplightTheme.colors.textTertiary)
            }
            EssentialModuleGroup(
                items = group.modules.map { it.toModuleItem(lang, strings, onAction) },
                expandedIds = state.expandedIds,
                labels = ModuleLabels(strings.detail.readMore, strings.detail.showLess, strings.detail.expanded, strings.detail.collapsed),
                reduceMotion = reduceMotion,
                onToggle = { onAction(EssentialsAction.ToggleItem(it)) }
            )
        }
    }
}

private fun EssentialGroupKind.glyph(): EssentialsGlyph = when (this) {
    EssentialGroupKind.REGISTRATION -> EssentialsGlyph.Document
    EssentialGroupKind.GETTING_AROUND -> EssentialsGlyph.Bus
    EssentialGroupKind.PRACTICAL -> EssentialsGlyph.Wallet
    EssentialGroupKind.LOCAL_TIPS -> EssentialsGlyph.Lightbulb
}

/** One CMS entry as an accordion row: a Map action per exact location, an Open action per link. */
private fun CityEssential.toModuleItem(lang: String, strings: AppStrings, onAction: (EssentialsAction) -> Unit): ModuleItem {
    val title = localizedTitle(lang)
    val locationActions = resolvedLocations(lang).mapNotNull { location ->
        val lat = location.latitude ?: return@mapNotNull null
        val lng = location.longitude ?: return@mapNotNull null
        val label = location.title.ifBlank { title }
        ModuleAction(EssentialsGlyph.Pin, label, strings.essentials.map) { onAction(EssentialsAction.OpenLocation(label, lat, lng)) }
    }
    val linkActions = links.filter { it.isNotBlank() }.map { url ->
        ModuleAction(EssentialsGlyph.ArrowUpRight, url.linkTitle(), strings.essentials.open) { onAction(EssentialsAction.OpenLink(url)) }
    }
    // No CMS summary field exists yet, so the subtitle stays hidden rather than showing link counts.
    return ModuleItem(id, title, subtitle = null, body = localizedContent(lang).trim(), actions = locationActions + linkActions)
}

@Composable
private fun UtilitiesView(
    state: EssentialsState,
    strings: AppStrings,
    onAction: (EssentialsAction) -> Unit
) {
    val s = strings.essentials
    val colors = LamplightTheme.colors
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
        LocationStatusLine(locationKnown = location != null, strings = strings, onTurnOn = { onAction(EssentialsAction.TurnOnLocation) })

        if (state.utilitiesFailed && state.sections.isEmpty()) {
            Text(text = s.couldntLoad.replace("{category}", s.utilities), style = LamplightType.bodyS(), color = HomePalette.onGradientSecondary)
            RetryPill(strings.retry) { onAction(EssentialsAction.Retry) }
            return@Column
        }
        if (state.sections.isEmpty()) return@Column

        // Only categories with records get a tile, so no shortcut leads to an empty map.
        val tileOrder = listOf(UtilityCategory.EMERGENCY) + UtilityCategory.entries.filter { it != UtilityCategory.EMERGENCY }
        UtilityCategoryGrid(
            tiles = tileOrder.filter { it in byCategory }.map { category ->
                UtilityTile(category.tileLabel(s), category.glyph(), emergency = category == UtilityCategory.EMERGENCY) {
                    onAction(EssentialsAction.OpenUtilityMap(category, null))
                }
            } + UtilityTile(s.tileAllOnMap, EssentialsGlyph.Map) { onAction(EssentialsAction.OpenUtilityMap(null, null)) },
            columns = columns
        )

        val summaries = SUMMARY_UTILITY_CATEGORIES.mapNotNull(byCategory::get)
        summaries.forEachIndexed { index, section ->
            val title = section.category.cardTitle(s)
            UtilitySummaryCard(
                title = title,
                nearbyLabel = section.nearbyText(strings),
                closestLabel = s.closest,
                closestDistance = location?.let { section.closestDistanceMeters?.formatDistance() },
                pins = section.previewUtilities.mapNotNull { u ->
                    val lat = u.latitude ?: return@mapNotNull null
                    val lng = u.longitude ?: return@mapNotNull null
                    UtilityMapPin(u.id, u.name, GeoPoint(lat, lng))
                },
                userLocation = location,
                mapDescription = s.mapPreviewDescription
                    .replace("{n}", section.previewUtilities.size.toString())
                    .replace("{category}", title),
                nearest = section.nearest.map { item ->
                    NearestRow(item.utility.id, item.utility.name, location?.let { item.distanceMeters?.formatDistance() })
                },
                openMapLabel = s.openMap,
                primary = index == 0,
                onOpenMap = { onAction(EssentialsAction.OpenUtilityMap(section.category, null)) },
                onOpenItem = { id -> onAction(EssentialsAction.OpenUtilityMap(section.category, id)) },
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        state.sections
            .filter { it.category !in SUMMARY_UTILITY_CATEGORIES }
            .forEach { section ->
                UtilityCompactRow(
                    title = section.category.cardTitle(s),
                    subtitle = section.nearbyText(strings),
                    glyph = section.category.glyph(),
                    closestDistance = location?.takeIf { section.nearbyCount > 0 }?.let { section.closestDistanceMeters?.formatDistance() },
                    onClick = { onAction(EssentialsAction.OpenUtilityMap(section.category, null)) }
                )
            }
    }
}

private fun UtilityCategorySection.nearbyText(strings: AppStrings): String =
    if (nearbyCount > 0) strings.essentials.nearbyCount.replace("{n}", nearbyCount.toString())
    else strings.essentials.noneNearby

@Composable
private fun LocationStatusLine(locationKnown: Boolean, strings: AppStrings, onTurnOn: () -> Unit) {
    val colors = LamplightTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(colors.lamp.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(colors.chalk))
        }
        if (locationKnown) {
            Text(text = strings.essentials.distancesCurrent, style = LamplightType.bodyS(), color = HomePalette.onGradientSecondary)
        } else {
            Column {
                Text(text = strings.essentials.locationOff, style = LamplightType.bodyS(), color = HomePalette.onGradientSecondary)
                Text(
                    text = strings.essentials.turnOn,
                    style = LamplightType.bodyS().copy(fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline),
                    color = colors.lamp,
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
        style = LamplightType.sectionTitleS(),
        color = LamplightTheme.colors.textPrimary,
        modifier = modifier.semantics { heading() }
    )
}

@Composable
private fun ConnectOnceMessage(strings: AppStrings, onRetry: () -> Unit) {
    Column(Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = strings.essentials.connectOnce, style = LamplightType.body(), color = LamplightTheme.colors.textSecondary)
        RetryPill(strings.retry, onRetry)
    }
}

@Composable
private fun RetryPill(label: String, onClick: () -> Unit) {
    val colors = LamplightTheme.colors
    Text(
        text = label,
        style = LamplightType.body().copy(fontWeight = FontWeight.SemiBold),
        color = colors.night,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(LamplightShapes.pill)
            .background(colors.lamp)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 13.dp)
    )
}
