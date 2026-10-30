package llc.bokadev.kompass.presentation.experiencedetail

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_activity
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.detail_best_time
import kompass.composeapp.generated.resources.detail_duration
import kompass.composeapp.generated.resources.detail_how_to_get_there
import kompass.composeapp.generated.resources.detail_open_in_maps
import kompass.composeapp.generated.resources.detail_visit_website
import kompass.composeapp.generated.resources.detail_where_it_is
import kompass.composeapp.generated.resources.redesign_must_see_saved
import kompass.composeapp.generated.resources.redesign_not_saved
import kompass.composeapp.generated.resources.redesign_remove_title_from_saved
import kompass.composeapp.generated.resources.redesign_save_title
import llc.bokadev.kompass.core.audio.NavAudioMode
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.appStringsFor
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.Experience
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailCompactBar
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailGlyph
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailHero
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPillButton
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRow
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailPracticalRows
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailTextSection
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailWhereItIs
import llc.bokadev.kompass.presentation.screens.placedetail.normalizedForCompare
import llc.bokadev.kompass.presentation.screens.placedetail.prettyLabel
import llc.bokadev.kompass.presentation.screens.placedetail.toDetailLabel
import llc.bokadev.kompass.presentation.screens.placedetail.toVisitLength
import llc.bokadev.kompass.presentation.shared.DetailListenControl
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource

/**
 * Lamplight activity detail: the same template as place detail (hero, type label, Gloock title,
 * practical rows, About, Where it is) plus activity-only sections (how to get there, website).
 * Every section shows only when its data exists. [audioMode] is the activity's free audio state
 * (null = no audio), played via [onListenClick].
 * The shared detail components still take the AppStrings tables until they are migrated.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ExperienceDetailScreenContent(
    modifier: Modifier = Modifier,
    state: ExperienceDetailState,
    currentLocation: GeoPoint?,
    audioMode: NavAudioMode?,
    onBackClick: () -> Unit,
    onSavedChange: (Boolean) -> Unit,
    onMapsClick: () -> Unit,
    onWebsiteClick: (String) -> Unit,
    onUserScrolled: () -> Unit,
    onListenClick: () -> Unit
) {
    val activity = state.activity ?: return
    val isFavorited = state.isSaved
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val s = appStringsFor(lang).detail
    val density = LocalDensity.current
    val reduceMotion = rememberReduceMotion()
    val scroll = rememberScrollState()
    val name = activity.localizedName(lang)
    val area = activity.localizedLocation(lang).trim().takeIf { it.isNotBlank() }
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
                photoUrls = activity.photos.map(::buildPhotoUrl),
                height = heroHeight,
                isSaved = isFavorited,
                backLabel = backLabel,
                saveLabel = saveLabel,
                savedStateLabel = savedState,
                photoOfLabel = s.photoOf,
                onBack = onBackClick,
                onToggleSave = onFavoriteClick,
                parallax = { if (reduceMotion) 0f else scroll.value.toFloat() }
            )
            TitleBlock(activity, name, area, lang) {
                audioMode?.let { DetailListenControl(it, s, reduceMotion, onListenClick) }
            }
            DetailPracticalRows(
                fullWidth = emptyList(),
                paired = listOfNotNull(
                    activity.durationMin?.toVisitLength(s)?.let { DetailPracticalRow(DetailGlyph.Visit, stringResource(Res.string.detail_duration), it) },
                    activity.bestTime.toDetailLabel(s)?.let { DetailPracticalRow(DetailGlyph.Sun, stringResource(Res.string.detail_best_time), it) }
                ),
                lang = lang
            )
            activity.externalWebsite?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
                DetailPillButton(
                    label = stringResource(Res.string.detail_visit_website),
                    glyph = DetailGlyph.ArrowUpRight,
                    filled = true,
                    height = 56.dp,
                    onClick = { onWebsiteClick(url) },
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
                )
            }
            activity.aboutText(lang)?.let { DetailTextSection(it, s) }
            activity.localizedHowToGetThere(lang).trim().takeIf { it.isNotBlank() }?.let {
                DetailTextSection(it, s, title = stringResource(Res.string.detail_how_to_get_there), collapsedLines = 8)
            }
            val lat = activity.latitude
            val lng = activity.longitude
            if (lat != null && lng != null) {
                DetailWhereItIs(
                    title = stringResource(Res.string.detail_where_it_is),
                    name = name,
                    destination = GeoPoint(lat, lng),
                    currentLocation = currentLocation,
                    areaLabel = area,
                    openInMapsLabel = stringResource(Res.string.detail_open_in_maps),
                    onOpenMaps = onMapsClick
                )
            }
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
private fun TitleBlock(activity: Experience, name: String, area: String?, lang: String, listen: @Composable () -> Unit) {
    val colors = KompassTheme.colors
    val type = activity.category?.trim()?.takeIf { it.isNotBlank() }?.prettyLabel() ?: stringResource(Res.string.detail_activity)
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

@Preview
@Composable
private fun ExperienceDetailScreenContentPreview() {
    KompassTheme {
        ExperienceDetailScreenContent(
            modifier = Modifier.pageGradientBackground(),
            state = ExperienceDetailState(
                isLoading = false,
                activity = Experience(
                    id = "preview", cityId = "kotor", name = mapOf("en" to "Sea kayak"),
                    description = mapOf("en" to "Paddle the bay at sunrise."), location = mapOf("en" to "Dobrota"),
                    audioFile = emptyMap(), photos = emptyList()
                )
            ),
            currentLocation = null,
            audioMode = null,
            onBackClick = {},
            onSavedChange = {},
            onMapsClick = {},
            onWebsiteClick = {},
            onUserScrolled = {},
            onListenClick = {}
        )
    }
}
