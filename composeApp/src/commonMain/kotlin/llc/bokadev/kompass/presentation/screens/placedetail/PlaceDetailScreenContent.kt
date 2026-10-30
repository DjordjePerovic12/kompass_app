package llc.bokadev.kompass.presentation.screens.placedetail

import llc.bokadev.kompass.presentation.shared.DetailListenControl
import llc.bokadev.kompass.core.audio.NavAudioMode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.presentation.mvi.BaseContentView
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.presentation.shared.icons.BookmarkIcon
import llc.bokadev.kompass.presentation.shared.icons.ChevronLeftIcon
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.detail.DetailHero
import llc.bokadev.kompass.presentation.shared.detail.DetailCompactBar
import llc.bokadev.kompass.presentation.shared.icons.DetailIcon
import llc.bokadev.kompass.presentation.shared.detail.DetailLoadState
import llc.bokadev.kompass.presentation.shared.detail.DetailPillButton
import llc.bokadev.kompass.presentation.shared.detail.DetailPracticalRow
import llc.bokadev.kompass.presentation.shared.detail.DetailPracticalRows
import llc.bokadev.kompass.presentation.shared.detail.DetailTextSection
import llc.bokadev.kompass.presentation.shared.detail.DetailWhereItIs
import llc.bokadev.kompass.presentation.shared.NativeDetailMapView
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import llc.bokadev.kompass.presentation.shared.detail.toDetailLabel
import llc.bokadev.kompass.presentation.shared.detail.toVisitLength

/**
 * Lamplight place detail (place detail spec Part 1). One template for every category: each
 * section shows only when its data exists, and none renders as an empty container. Audio is
 * played from the Listen control under the title ([audioMode]); the center button opens the library.
 */
@Composable
fun PlaceDetailScreenContent(
    state: PlaceDetailState,
    currentLocation: GeoPoint?,
    onAction: (PlaceDetailAction) -> Unit,
    relatedActivities: @Composable () -> Unit = {},
    /** The place's free audio state (null = no audio); played via [PlaceDetailAction.Listen]. */
    audioMode: NavAudioMode? = null
) {
    // The page is `night` from top to bottom, so the status bar icons stay light throughout.
    StatusBarIconsEffect(lightIcons = true)
    val strings = rememberAppStrings()
    val onRetry = { onAction(PlaceDetailAction.Retry) }
    val onBack = { onAction(PlaceDetailAction.Back) }
    BaseContentView(
        state = state,
        onRetry = onRetry,
        modifier = Modifier.pageGradientBackground(),
        loading = { DetailLoadState(null, strings.retry, strings.detail.back, onRetry, onBack) },
        error = { message -> DetailLoadState(message, strings.retry, strings.detail.back, onRetry, onBack) }
    ) {
        val place = state.place
        when {
            place != null -> DetailBody(
                place = place,
                isFavorited = state.isSaved,
                currentLocation = currentLocation,
                onFavoriteClick = { onAction(PlaceDetailAction.SetSaved(!state.isSaved)) },
                onBack = onBack,
                onOpenMaps = { onAction(PlaceDetailAction.OpenMaps) },
                onOpenMenu = { onAction(PlaceDetailAction.OpenMenu(it)) },
                onCall = { onAction(PlaceDetailAction.Call(it)) },
                onUserScrolled = { onAction(PlaceDetailAction.UserScrolled) },
                relatedActivities = relatedActivities,
                audioMode = audioMode,
                onListen = { onAction(PlaceDetailAction.Listen) }
            )
            else -> Unit
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DetailBody(
    place: Place,
    isFavorited: Boolean,
    currentLocation: GeoPoint?,
    onFavoriteClick: () -> Unit,
    onBack: () -> Unit,
    onOpenMaps: (Place) -> Unit,
    onOpenMenu: (String) -> Unit,
    onCall: (String) -> Unit,
    onUserScrolled: () -> Unit,
    relatedActivities: @Composable () -> Unit,
    audioMode: NavAudioMode?,
    onListen: () -> Unit
) {
    val lang = currentAppLanguage()
    val strings = rememberAppStrings()
    val colors = KompassTheme.colors
    val density = LocalDensity.current
    val reduceMotion = rememberReduceMotion()
    val scroll = rememberScrollState()
    val name = place.localizedName(lang)
    val saveLabel = (if (isFavorited) strings.redesign.removeTitleFromSaved else strings.redesign.saveTitle).replace("{title}", name)
    val savedState = if (isFavorited) strings.redesign.mustSeeSaved else strings.redesign.notSaved

    val screenHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
    // About two-fifths of the screen (260–420dp): the title and first facts stay in the first view.
    val heroHeight = (screenHeight * 0.4f).coerceIn(260.dp, 420.dp)
    val compactBarVisible by remember(heroHeight) {
        derivedStateOf { scroll.value > with(density) { (heroHeight - 120.dp).toPx() } }
    }
    LaunchedEffect(scroll.isScrollInProgress) { if (scroll.isScrollInProgress) onUserScrolled() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            DetailHero(
                photoUrls = place.photos.map(::buildPhotoUrl),
                height = heroHeight,
                isSaved = isFavorited,
                backLabel = strings.detail.back,
                saveLabel = saveLabel,
                savedStateLabel = savedState,
                photoOfLabel = strings.detail.photoOf,
                onBack = onBack,
                onToggleSave = onFavoriteClick,
                parallax = { if (reduceMotion) 0f else scroll.value.toFloat() }
            )
            TitleBlock(place, lang, strings) {
                audioMode?.let { DetailListenControl(it, strings.detail, reduceMotion, onListen) }
            }
            PracticalRows(place, lang, strings)
            EatAndDrinkActions(place, strings, onOpenMenu, onCall)
            place.detailAboutText(lang)?.let { DetailTextSection(it, strings.detail) }
            place.detailLocalTip(lang)?.let { LocalTipPanel(it, lang, strings) }
            relatedActivities()
            DetailWhereItIs(
                title = strings.detail.whereItIs,
                name = name,
                destination = GeoPoint(place.latitude, place.longitude),
                currentLocation = currentLocation,
                areaLabel = place.detailAreaLabel(),
                openInMapsLabel = strings.detail.openInMaps,
                onOpenMaps = { onOpenMaps(place) }
            )
            // The shell already insets content above the floating bar; this is the spec's breathing room.
            Spacer(Modifier.height(26.dp))
        }

        AnimatedVisibility(visible = compactBarVisible, enter = fadeIn(), exit = fadeOut()) {
            DetailCompactBar(
                title = name,
                backLabel = strings.detail.back,
                onBack = onBack,
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
    val hours = place.detailOpeningHours(lang)?.let { DetailPracticalRow(DetailGlyph.Clock, s.openingHours, it) }
    // Only a relative price indicator exists, so it is shown as a price level for places to eat
    // and never presented as an entrance fee for sights (spec §1.4).
    val price = place.priceIndicator?.takeIf { eatAndDrink }
        ?.let { DetailPracticalRow(DetailGlyph.Price, s.priceLevel, it.toEuros(), s.priceCompared) }
    val visit = place.estimatedDuration?.toVisitLength(s)?.let { DetailPracticalRow(DetailGlyph.Visit, s.visitTakes, it) }
    val best = place.bestTime.toDetailLabel(s)?.let { DetailPracticalRow(DetailGlyph.Sun, s.bestTime, it) }
    DetailPracticalRows(fullWidth = listOfNotNull(hours, price), paired = listOfNotNull(visit, best), lang = lang)
}

@Composable
private fun EatAndDrinkActions(place: Place, strings: AppStrings, onOpenMenu: (String) -> Unit, onCall: (String) -> Unit) {
    if (place.category != PlaceCategory.EAT_AND_DRINK) return
    val menuUrl = place.menuUrl?.trim()?.takeIf { it.isNotBlank() }
    val phone = place.bookingPhone?.trim()?.takeIf { it.isNotBlank() }
    if (menuUrl == null && phone == null) return
    val colors = KompassTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            menuUrl?.let { url ->
                DetailPillButton(
                    label = strings.detail.viewMenu,
                    glyph = DetailGlyph.ArrowUpRight,
                    filled = true,
                    height = 56.dp,
                    onClick = { onOpenMenu(url) },
                    modifier = Modifier.weight(1f)
                )
            }
            phone?.let {
                DetailPillButton(
                    label = strings.detail.callToBook,
                    glyph = DetailGlyph.Phone,
                    filled = false,
                    height = 56.dp,
                    onClick = { onCall(it) },
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
private fun LocalTipPanel(tip: String, lang: String, strings: AppStrings) {
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
            Text(text = strings.detail.localTip.uppercaseFor(lang), style = KompassTheme.typography.geologicaSemiBold11, color = colors.nightBlack)
        }
        Text(text = tip, style = KompassTheme.typography.gloockRegular23.copy(fontSize = 21.sp, lineHeight = 26.sp), color = colors.nightBlack)
    }
}
