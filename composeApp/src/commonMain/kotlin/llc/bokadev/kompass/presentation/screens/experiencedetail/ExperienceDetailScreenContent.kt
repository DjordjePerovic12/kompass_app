package llc.bokadev.kompass.presentation.screens.experiencedetail

import llc.bokadev.kompass.presentation.shared.DetailListenControl
import llc.bokadev.kompass.core.audio.NavAudioMode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.core.presentation.mvi.BaseContentView
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.currentAppLanguage
import llc.bokadev.kompass.core.utils.rememberAppStrings
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailCompactBar
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailGlyph
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailHero
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailLoadState
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPillButton
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRow
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRows
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailTextSection
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailWhereItIs
import llc.bokadev.kompass.presentation.screens.placedetail.normalizedForCompare
import llc.bokadev.kompass.presentation.screens.placedetail.prettyLabel
import llc.bokadev.kompass.presentation.screens.placedetail.toDetailLabel
import llc.bokadev.kompass.presentation.screens.placedetail.toVisitLength
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground

/**
 * Lamplight activity detail: the same template as place detail (hero, type label, Gloock title,
 * practical rows, About, Where it is) plus activity-only sections (how to get there, website).
 * Every section shows only when its data exists. Audio plays from the floating bar's center button.
 */
@Composable
fun ExperienceDetailScreenContent(
    state: ExperienceDetailState,
    currentLocation: GeoPoint?,
    onAction: (ExperienceDetailAction) -> Unit,
    /** The activity's free audio state (null = no audio); played via [ExperienceDetailAction.Listen]. */
    audioMode: NavAudioMode? = null
) {
    StatusBarIconsEffect(lightIcons = true)
    val strings = rememberAppStrings()
    val onRetry = { onAction(ExperienceDetailAction.Retry) }
    val onBack = { onAction(ExperienceDetailAction.Back) }
    BaseContentView(
        state = state,
        onRetry = onRetry,
        modifier = Modifier.pageGradientBackground(),
        loading = { DetailLoadState(null, strings.retry, strings.detail.back, onRetry, onBack) },
        error = { message -> DetailLoadState(message, strings.retry, strings.detail.back, onRetry, onBack) }
    ) {
        state.activity?.let { activity ->
            DetailBody(
                activity, state.isSaved, currentLocation, strings,
                { onAction(ExperienceDetailAction.SetSaved(!state.isSaved)) },
                onBack,
                { onAction(ExperienceDetailAction.OpenMaps) },
                { onAction(ExperienceDetailAction.OpenWebsite(it)) },
                { onAction(ExperienceDetailAction.UserScrolled) },
                audioMode,
                { onAction(ExperienceDetailAction.Listen) }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DetailBody(
    activity: Experience,
    isFavorited: Boolean,
    currentLocation: GeoPoint?,
    strings: AppStrings,
    onFavoriteClick: () -> Unit,
    onBack: () -> Unit,
    onOpenMaps: (Experience) -> Unit,
    onOpenWebsite: (String) -> Unit,
    onUserScrolled: () -> Unit,
    audioMode: NavAudioMode?,
    onListen: () -> Unit
) {
    val lang = currentAppLanguage()
    val density = LocalDensity.current
    val reduceMotion = rememberReduceMotion()
    val scroll = rememberScrollState()
    val s = strings.detail
    val name = activity.localizedName(lang)
    val area = activity.localizedLocation(lang).trim().takeIf { it.isNotBlank() }
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
                photoUrls = activity.photos.map(::buildPhotoUrl),
                height = heroHeight,
                isSaved = isFavorited,
                backLabel = s.back,
                saveLabel = saveLabel,
                savedStateLabel = savedState,
                photoOfLabel = s.photoOf,
                onBack = onBack,
                onToggleSave = onFavoriteClick,
                parallax = { if (reduceMotion) 0f else scroll.value.toFloat() }
            )
            TitleBlock(activity, name, area, lang, strings) {
                audioMode?.let { DetailListenControl(it, strings.detail, reduceMotion, onListen) }
            }
            DetailPracticalRows(
                fullWidth = emptyList(),
                paired = listOfNotNull(
                    activity.durationMin?.toVisitLength(s)?.let { DetailPracticalRow(DetailGlyph.Visit, s.duration, it) },
                    activity.bestTime.toDetailLabel(s)?.let { DetailPracticalRow(DetailGlyph.Sun, s.bestTime, it) }
                ),
                lang = lang
            )
            activity.externalWebsite?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
                DetailPillButton(
                    label = s.visitWebsite,
                    glyph = DetailGlyph.ArrowUpRight,
                    filled = true,
                    height = 56.dp,
                    onClick = { onOpenWebsite(url) },
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
                )
            }
            activity.aboutText(lang)?.let { DetailTextSection(it, s) }
            activity.localizedHowToGetThere(lang).trim().takeIf { it.isNotBlank() }?.let {
                DetailTextSection(it, s, title = s.howToGetThere, collapsedLines = 8)
            }
            val lat = activity.latitude
            val lng = activity.longitude
            if (lat != null && lng != null) {
                DetailWhereItIs(
                    title = s.whereItIs,
                    name = name,
                    destination = GeoPoint(lat, lng),
                    currentLocation = currentLocation,
                    areaLabel = area,
                    openInMapsLabel = s.openInMaps,
                    onOpenMaps = { onOpenMaps(activity) }
                )
            }
            // The shell already insets content above the floating bar; this is the spec's breathing room.
            Spacer(Modifier.height(26.dp))
        }

        AnimatedVisibility(visible = compactBarVisible, enter = fadeIn(), exit = fadeOut()) {
            DetailCompactBar(
                title = name,
                backLabel = s.back,
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
private fun TitleBlock(activity: Experience, name: String, area: String?, lang: String, strings: AppStrings, listen: @Composable () -> Unit) {
    val colors = KompassTheme.colors
    val type = activity.category?.trim()?.takeIf { it.isNotBlank() }?.prettyLabel() ?: strings.detail.activity
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // The CMS location is often a sentence; only a short place name belongs in the label
        // (the full text stays under "Where it is").
        val shortArea = area?.takeIf { it.length <= 24 && !it.contains(',') }
        Text(text = listOfNotNull(type, shortArea).joinToString(" · ").uppercaseFor(lang), style = KompassTheme.typography.geologicaSemiBold11, color = colors.lampYellow)
        Text(
            text = name,
            style = KompassTheme.typography.gloockRegular34.copy(fontSize = 40.sp, lineHeight = 41.sp),
            color = colors.boneWhite,
            modifier = Modifier.semantics { heading() }
        )
        // Right under the title: the first view always shows how to listen.
        listen()
        activity.localizedDescription(lang).trim().takeIf { it.isNotBlank() }?.let { short ->
            Text(text = short, style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.Normal, lineHeight = 24.sp), color = KompassTheme.colors.white85)
        }
    }
}

/** The long description, unless it is blank or only repeats the short one under the title. */
private fun Experience.aboutText(lang: String): String? {
    val long = localizedLongDescription(lang).trim().takeIf { it.isNotBlank() } ?: return null
    return long.takeUnless { it.normalizedForCompare() in localizedDescription(lang).normalizedForCompare() }
}
