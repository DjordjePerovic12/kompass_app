package llc.bokadev.kompass.presentation.placedetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.detail_best_time
import kompass.composeapp.generated.resources.detail_call_to_book
import kompass.composeapp.generated.resources.detail_local_tip
import kompass.composeapp.generated.resources.detail_open_in_maps
import kompass.composeapp.generated.resources.detail_opening_hours
import kompass.composeapp.generated.resources.detail_price_compared
import kompass.composeapp.generated.resources.detail_price_level
import kompass.composeapp.generated.resources.detail_view_menu
import kompass.composeapp.generated.resources.detail_visit_takes
import kompass.composeapp.generated.resources.detail_where_it_is
import kompass.composeapp.generated.resources.redesign_must_see_saved
import kompass.composeapp.generated.resources.redesign_not_saved
import kompass.composeapp.generated.resources.redesign_remove_title_from_saved
import kompass.composeapp.generated.resources.redesign_save_title
import llc.bokadev.kompass.core.audio.NavAudioMode
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.model.PriceIndicator
import llc.bokadev.kompass.presentation.placedetail.utils.detailAboutText
import llc.bokadev.kompass.presentation.placedetail.utils.detailAreaLabel
import llc.bokadev.kompass.presentation.placedetail.utils.detailLocalTip
import llc.bokadev.kompass.presentation.placedetail.utils.detailOpeningHours
import llc.bokadev.kompass.presentation.placedetail.utils.detailTypeLabel
import llc.bokadev.kompass.presentation.placedetail.utils.toEuros
import llc.bokadev.kompass.presentation.shared.DetailListenControl
import llc.bokadev.kompass.presentation.shared.detail.DetailCompactBar
import llc.bokadev.kompass.presentation.shared.detail.DetailHero
import llc.bokadev.kompass.presentation.shared.detail.DetailPillButton
import llc.bokadev.kompass.presentation.shared.detail.DetailPracticalRow
import llc.bokadev.kompass.presentation.shared.detail.DetailPracticalRows
import llc.bokadev.kompass.presentation.shared.detail.DetailTextSection
import llc.bokadev.kompass.presentation.shared.detail.DetailWhereItIs
import llc.bokadev.kompass.presentation.shared.detail.toDetailLabel
import llc.bokadev.kompass.presentation.shared.detail.toVisitLength
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.icons.DetailIcon
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource

/**
 * Lamplight place detail (place detail spec Part 1). One template for every category: each
 * section shows only when its data exists, and none renders as an empty container. [audioMode]
 * is the place's free audio state (null = no audio), played via [onListenClick]; the center
 * button opens the library. [relatedActivities] is the embedded related-activities section.
 * The shared detail components still take the AppStrings tables until they are migrated.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaceDetailScreenContent(
    modifier: Modifier = Modifier,
    state: PlaceDetailState,
    currentLocation: GeoPoint?,
    audioMode: NavAudioMode?,
    onBackClick: () -> Unit,
    onSavedChange: (Boolean) -> Unit,
    onMapsClick: () -> Unit,
    onMenuClick: (String) -> Unit,
    onCallClick: (String) -> Unit,
    onUserScrolled: () -> Unit,
    onListenClick: () -> Unit,
    relatedActivities: @Composable () -> Unit = {}
) {
    val place = state.place ?: return
    val isFavorited = state.isSaved
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val strings = appStringsFor(lang)
    val density = LocalDensity.current
    val reduceMotion = rememberReduceMotion()
    val scroll = rememberScrollState()
    val name = place.localizedName(lang)
    val saveLabel = stringResource(if (isFavorited) Res.string.redesign_remove_title_from_saved else Res.string.redesign_save_title, name)
    val savedState = stringResource(if (isFavorited) Res.string.redesign_must_see_saved else Res.string.redesign_not_saved)
    val backLabel = stringResource(Res.string.detail_back)
    val onFavoriteClick = { onSavedChange(!isFavorited) }

    val screenHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
    // About two-fifths of the screen (260–420dp): the title and first facts stay in the first view.
    val heroHeight = (screenHeight * 0.4f).coerceIn(260.dp, 420.dp)
    val compactBarVisible by remember(heroHeight) {
        derivedStateOf { scroll.value > with(density) { (heroHeight - 120.dp).toPx() } }
    }
    LaunchedEffect(scroll.isScrollInProgress) { if (scroll.isScrollInProgress) onUserScrolled() }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            DetailHero(
                photoUrls = place.photos.map(::buildPhotoUrl),
                height = heroHeight,
                isSaved = isFavorited,
                backLabel = backLabel,
                saveLabel = saveLabel,
                savedStateLabel = savedState,
                photoOfLabel = strings.detail.photoOf,
                onBack = onBackClick,
                onToggleSave = onFavoriteClick,
                parallax = { if (reduceMotion) 0f else scroll.value.toFloat() }
            )
            TitleBlock(place, lang, strings) {
                audioMode?.let { DetailListenControl(it, strings.detail, reduceMotion, onListenClick) }
            }
            PracticalRows(place, lang, strings)
            EatAndDrinkActions(place, onMenuClick, onCallClick)
            place.detailAboutText(lang)?.let { DetailTextSection(it, strings.detail) }
            place.detailLocalTip(lang)?.let { LocalTipPanel(it, lang) }
            relatedActivities()
            DetailWhereItIs(
                title = stringResource(Res.string.detail_where_it_is),
                name = name,
                destination = GeoPoint(place.latitude, place.longitude),
                currentLocation = currentLocation,
                areaLabel = place.detailAreaLabel(),
                openInMapsLabel = stringResource(Res.string.detail_open_in_maps),
                onOpenMaps = onMapsClick
            )
            // The shell already insets content above the floating bar; this is the spec's breathing room.
            Spacer(Modifier.height(26.dp))
        }

        AnimatedVisibility(visible = compactBarVisible, enter = fadeIn(), exit = fadeOut()) {
            DetailCompactBar(
                title = name,
                backLabel = backLabel,
                onBack = onBackClick,
                isSaved = isFavorited,
                saveLabel = saveLabel,
                savedStateLabel = savedState,
                onToggleSave = onFavoriteClick
            )
        }
    }
}

@Composable
private fun TitleBlock(place: Place, lang: String, strings: AppStrings, listen: @Composable () -> Unit) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = place.detailTypeLabel(strings, lang), style = KompassTheme.typography.geologicaSemiBold11, color = colors.lampYellow)
        Text(
            text = place.localizedName(lang),
            style = KompassTheme.typography.gloockRegular34.copy(fontSize = 40.sp, lineHeight = 41.sp),
            color = colors.boneWhite,
            modifier = Modifier.semantics { heading() }
        )
        // Right under the title: the first view always shows how to listen.
        listen()
        place.localizedDescription(lang).trim().takeIf { it.isNotBlank() }?.let { short ->
            Text(
                text = short,
                style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.Normal, lineHeight = 24.sp),
                color = KompassTheme.colors.white85
            )
        }
    }
}

@Composable
private fun PracticalRows(place: Place, lang: String, strings: AppStrings) {
    val s = strings.detail
    val eatAndDrink = place.category == PlaceCategory.EAT_AND_DRINK
    val hoursLabel = stringResource(Res.string.detail_opening_hours)
    val priceLabel = stringResource(Res.string.detail_price_level)
    val priceCompared = stringResource(Res.string.detail_price_compared)
    val visitLabel = stringResource(Res.string.detail_visit_takes)
    val bestLabel = stringResource(Res.string.detail_best_time)
    val hours = place.detailOpeningHours(lang)?.let { DetailPracticalRow(DetailGlyph.Clock, hoursLabel, it) }
    // Only a relative price indicator exists, so it is shown as a price level for places to eat
    // and never presented as an entrance fee for sights (spec §1.4).
    val price = place.priceIndicator?.takeIf { eatAndDrink }
        ?.let { DetailPracticalRow(DetailGlyph.Price, priceLabel, it.toEuros(), priceCompared) }
    val visit = place.estimatedDuration?.toVisitLength(s)?.let { DetailPracticalRow(DetailGlyph.Visit, visitLabel, it) }
    val best = place.bestTime.toDetailLabel(s)?.let { DetailPracticalRow(DetailGlyph.Sun, bestLabel, it) }
    DetailPracticalRows(fullWidth = listOfNotNull(hours, price), paired = listOfNotNull(visit, best), lang = lang)
}

@Composable
private fun EatAndDrinkActions(place: Place, onMenuClick: (String) -> Unit, onCallClick: (String) -> Unit) {
    if (place.category != PlaceCategory.EAT_AND_DRINK) return
    val menuUrl = place.menuUrl?.trim()?.takeIf { it.isNotBlank() }
    val phone = place.bookingPhone?.trim()?.takeIf { it.isNotBlank() }
    if (menuUrl == null && phone == null) return

    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            menuUrl?.let { url ->
                DetailPillButton(
                    label = stringResource(Res.string.detail_view_menu),
                    glyph = DetailGlyph.ArrowUpRight,
                    filled = true,
                    height = 56.dp,
                    onClick = { onMenuClick(url) },
                    modifier = Modifier.weight(1f)
                )
            }
            phone?.let {
                DetailPillButton(
                    label = stringResource(Res.string.detail_call_to_book),
                    glyph = DetailGlyph.Phone,
                    filled = false,
                    height = 56.dp,
                    onClick = { onCallClick(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        phone?.let {
            Text(
                text = it,
                style = KompassTheme.typography.geologicaRegular13,
                color = KompassTheme.colors.white85,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** The page's only yellow panel (spec §1.6). */
@Composable
private fun LocalTipPanel(tip: String, lang: String) {
    val colors = KompassTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.lampYellow)
            .padding(18.dp)
            .semantics(mergeDescendants = true) { },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DetailIcon(DetailGlyph.Lightbulb, color = colors.nightBlack, size = 16.dp)
            Text(text = stringResource(Res.string.detail_local_tip).uppercaseFor(lang), style = KompassTheme.typography.geologicaSemiBold11, color = colors.nightBlack)
        }
        Text(text = tip, style = KompassTheme.typography.gloockRegular23.copy(fontSize = 21.sp, lineHeight = 26.sp), color = colors.nightBlack)
    }
}

@Preview
@Composable
private fun PlaceDetailScreenContentPreview() {
    KompassTheme {
        PlaceDetailScreenContent(
            modifier = Modifier.pageGradientBackground(),
            state = PlaceDetailState(
                isLoading = false,
                place = Place(
                    id = "preview", cityId = "kotor", name = mapOf("en" to "Konoba Scala Santa"),
                    description = mapOf("en" to "Grilled fish in a stone house off the square."),
                    longDescription = mapOf("en" to "Family-run since 1987, a few steps from the Cathedral."),
                    localsTip = mapOf("en" to "Ask for the catch of the day."),
                    category = PlaceCategory.EAT_AND_DRINK, subCategory = "restaurant", priceIndicator = PriceIndicator.MODERATE,
                    latitude = 42.4247, longitude = 18.7712, zone = "old_town", tags = emptyList(),
                    bestTime = BestTime.EVENING, estimatedDuration = 90, openingHours = mapOf("en" to "12:00–23:00"),
                    menuUrl = "https://example.com/menu", bookingPhone = "+382 32 000 000", photos = emptyList(),
                    audioFile = null, audioAccessTier = "free", detailAccessTier = "free",
                    isMustSee = false, isActive = true, sortOrder = 0
                )
            ),
            currentLocation = null,
            audioMode = null,
            onBackClick = {},
            onSavedChange = {},
            onMapsClick = {},
            onMenuClick = {},
            onCallClick = {},
            onUserScrolled = {},
            onListenClick = {}
        )
    }
}
