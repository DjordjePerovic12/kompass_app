package llc.bokadev.kompass.presentation.screens.localfinds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.core.audio.NavAudioMode
import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.StoriesStrings
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.toClockDuration
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.presentation.screens.essentials.components.EssentialsGlyph
import llc.bokadev.kompass.presentation.screens.essentials.components.EssentialsIcon
import llc.bokadev.kompass.presentation.screens.home.components.ChevronLeftIcon
import llc.bokadev.kompass.presentation.screens.localfinds.components.DropCapParagraph
import llc.bokadev.kompass.presentation.screens.localfinds.components.NextStoryCard
import llc.bokadev.kompass.presentation.shared.ListenPill
import llc.bokadev.kompass.presentation.shared.toDisc
import llc.bokadev.kompass.presentation.screens.localfinds.components.StoryLocationRow
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailCompactBar
import llc.bokadev.kompass.presentation.screens.placedetail.components.DetailLoadState
import llc.bokadev.kompass.presentation.screens.search.components.SearchGlyph
import llc.bokadev.kompass.presentation.screens.search.components.SearchGlyphIcon
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground

private data class StoryDetailActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onListen: () -> Unit,
    val onOpenMap: (lat: Double, lng: Double, title: String) -> Unit,
    val onNext: (String) -> Unit,
    val onUserScrolled: () -> Unit
)

/**
 * Lamplight story detail (stories spec Part 2): reading first. Hero (or a compact `lamp` header),
 * title block, the body with a drop cap, where it happened, next story. No save, no map embed.
 */
@Composable
fun LocalFindDetailScreenContent(
    state: LocalFindDetailState,
    /** The center button's mode for this story's track; null without audio. */
    audioMode: NavAudioMode?,
    strings: AppStrings,
    lang: String,
    onAction: (LocalFindDetailAction) -> Unit
) {
    val actions = StoryDetailActions(
        onBack = { onAction(LocalFindDetailAction.Back) },
        onRetry = { onAction(LocalFindDetailAction.Retry) },
        onListen = { onAction(LocalFindDetailAction.Listen) },
        onOpenMap = { lat, lng, title -> onAction(LocalFindDetailAction.OpenMap(lat, lng, title)) },
        onNext = { onAction(LocalFindDetailAction.OpenNext(it)) },
        onUserScrolled = { onAction(LocalFindDetailAction.UserScrolled) }
    )
    val find = state.find
    Box(Modifier.fillMaxSize().pageGradientBackground()) {
        if (find != null) {
            StoryBody(find, state.next, audioMode, strings, lang, actions)
        } else {
            StatusBarIconsEffect(lightIcons = true)
            DetailLoadState(
                message = if (state.isLoading) null else state.error?.resolve(strings),
                retryLabel = strings.retry,
                backLabel = strings.detail.back,
                onRetry = actions.onRetry,
                onBack = actions.onBack
            )
        }
    }
}

@Composable
private fun StoryBody(find: LocalFind, next: NextStory?, audioMode: NavAudioMode?, strings: AppStrings, lang: String, actions: StoryDetailActions) {
    val s = strings.stories
    val colors = KompassTheme.colors
    val density = LocalDensity.current
    val reduceMotion = rememberReduceMotion()
    val scroll = rememberScrollState()
    val title = find.localizedTitle(lang)
    val body = find.localizedBody(lang)
    val paragraphs = remember(body) { storyParagraphs(body) }
    val location = find.localizedLocation(lang)
    val photo = find.photos.firstOrNull { it.isNotBlank() }
    val heroHeight = 380.dp
    val compactVisible by remember(photo) {
        derivedStateOf { scroll.value > with(density) { (if (photo != null) heroHeight - 80.dp else 120.dp).toPx() } }
    }
    LaunchedEffect(scroll.isScrollInProgress) { if (scroll.isScrollInProgress) actions.onUserScrolled() }
    StatusBarIconsEffect(lightIcons = photo != null || compactVisible)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            if (photo != null) Hero(buildPhotoUrl(photo), heroHeight, strings.detail.back, actions.onBack)
            else CompactHeader(strings.detail.back, actions.onBack)

            TitleBlock(
                find = find,
                title = title,
                readMinutes = readMinutes(body),
                placeName = storyPlaceName(location),
                audioMode = audioMode,
                s = s,
                lang = lang,
                reduceMotion = reduceMotion,
                onListen = actions.onListen,
                // The block overlaps the hero by 24dp (spec §2.1); without a photo it sits below the header.
                modifier = if (photo != null) Modifier.overlapUp(24.dp) else Modifier.padding(top = 20.dp)
            )

            Column(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 22.dp).semantics { paneTitle = s.storyRegion },
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                if (paragraphs.isEmpty() && audioMode != null) {
                    // Only a title + audio: the listen block is the story.
                    Text(s.listenToStory, style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite)
                }
                val bodyStyle = KompassTheme.typography.geologicaMedium16.copy(fontSize = 18.sp, lineHeight = 30.6.sp, fontWeight = FontWeight.Normal)
                paragraphs.forEachIndexed { index, paragraph ->
                    val quote = pullQuoteOf(paragraph)
                    when {
                        quote != null -> PullQuote(quote)
                        index == 0 -> DropCapParagraph(
                            text = paragraph,
                            style = bodyStyle,
                            color = colors.boneWhite,
                            capStyle = KompassTheme.typography.gloockRegular40.copy(fontSize = 64.sp, lineHeight = 64.sp),
                            capColor = colors.lampYellow,
                            enabled = density.fontScale < 1.3f
                        )
                        else -> Text(paragraph, style = bodyStyle, color = colors.boneWhite)
                    }
                }
            }

            WhereItHappened(find, location, title, s, actions.onOpenMap)

            next?.let {
                NextStoryCard(
                    label = s.nextStory.uppercaseFor(lang),
                    title = it.title,
                    onClick = { actions.onNext(it.id) },
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 30.dp)
                )
            }
            // The shell already insets content above the floating bar; this is breathing room.
            Spacer(Modifier.height(26.dp))
        }

        AnimatedVisibility(visible = compactVisible, enter = fadeIn(), exit = fadeOut()) {
            Column {
                DetailCompactBar(title = title, backLabel = strings.detail.back, onBack = actions.onBack)
                // Reading progress: a thin `lamp` line under the bar.
                val progress = if (scroll.maxValue > 0) scroll.value.toFloat() / scroll.maxValue else 0f
                Box(Modifier.fillMaxWidth().height(2.dp).background(colors.nightBlack)) {
                    Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(colors.lampYellow))
                }
            }
        }
    }
}

/** Lays the content out [by] higher, taking that much less space (an overlap that doesn't leave a gap). */
private fun Modifier.overlapUp(by: androidx.compose.ui.unit.Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val shift = by.roundToPx()
    layout(placeable.width, (placeable.height - shift).coerceAtLeast(0)) { placeable.place(0, -shift) }
}

@Composable
private fun Hero(url: String, height: androidx.compose.ui.unit.Dp, backLabel: String, onBack: () -> Unit) {
    val colors = KompassTheme.colors
    Box(Modifier.fillMaxWidth().height(height).background(colors.slateDark)) {
        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxWidth().height(120.dp).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent))))
        Box(
            Modifier.fillMaxWidth().height(140.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, colors.nightBlack.copy(alpha = 0.85f))))
        )
        BackButton(backLabel, onBack, Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(start = 16.dp, top = 8.dp))
    }
}

@Composable
private fun CompactHeader(backLabel: String, onBack: () -> Unit) {
    val colors = KompassTheme.colors
    Box(Modifier.fillMaxWidth().background(colors.lampYellow)) {
        Text(
            "“",
            style = KompassTheme.typography.gloockRegular40.copy(fontSize = 120.sp, lineHeight = 120.sp),
            color = colors.nightBlack.copy(alpha = 0.14f),
            modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.statusBars).padding(end = 18.dp)
        )
        BackButton(backLabel, onBack, Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(start = 20.dp, top = 8.dp, bottom = 22.dp))
    }
}

@Composable
private fun BackButton(label: String, onBack: () -> Unit, modifier: Modifier) {
    val colors = KompassTheme.colors
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.nightBlack)
            .clickable(onClick = onBack)
            .semantics { role = Role.Button; contentDescription = label },
        contentAlignment = Alignment.Center
    ) { ChevronLeftIcon(color = colors.chalkWhite, size = 18.dp) }
}

@Composable
private fun TitleBlock(
    find: LocalFind,
    title: String,
    readMinutes: Int?,
    placeName: String?,
    audioMode: NavAudioMode?,
    s: StoriesStrings,
    lang: String,
    reduceMotion: Boolean,
    onListen: () -> Unit,
    modifier: Modifier
) {
    val colors = KompassTheme.colors
    val type = if (find.storyKind == StoryKind.LOCALISM) s.localism else s.legend
    val label = listOfNotNull(type, storyArea(find.localizedLocation(lang))).joinToString(" · ").uppercaseFor(lang)
    Column(modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, style = KompassTheme.typography.geologicaSemiBold11, color = colors.lampYellow)
        Text(
            title,
            style = KompassTheme.typography.gloockRegular34.copy(fontSize = 40.sp, lineHeight = 41.sp),
            color = colors.boneWhite,
            modifier = Modifier.semantics { heading() }
        )
        if (readMinutes != null || placeName != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                readMinutes?.let {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        SearchGlyphIcon(SearchGlyph.Book, colors.stoneGray, 14.dp)
                        Text(s.minRead.replace("{n}", it.toString()), style = KompassTheme.typography.geologicaRegular13, color = KompassTheme.colors.white85)
                    }
                }
                placeName?.let {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        EssentialsIcon(EssentialsGlyph.Pin, colors.stoneGray, 14.dp)
                        Text(it, style = KompassTheme.typography.geologicaRegular13, color = KompassTheme.colors.white85, maxLines = 1)
                    }
                }
            }
        }
        audioMode?.let { mode ->
            val duration = (mode as? NavAudioMode.Ready)?.track?.durationMs?.toClockDuration()
            val pillLabel = duration?.let { s.listenToStoryDuration.replace("{d}", it) } ?: s.listenToStory
            ListenPill(
                label = pillLabel,
                disc = mode.toDisc(),
                unavailable = mode is NavAudioMode.Unavailable,
                description = when (mode) {
                    is NavAudioMode.Playing -> s.pauseStory
                    is NavAudioMode.Loading -> s.loadingStory
                    else -> pillLabel
                },
                reduceMotion = reduceMotion,
                onClick = onListen,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** Hairlines top and bottom, a `lamp` “, Gloock 26/31. No left accent bar. */
@Composable
private fun PullQuote(text: String) {
    val colors = KompassTheme.colors
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.slateDivider))
        Text("“", style = KompassTheme.typography.gloockRegular40.copy(fontSize = 44.sp, lineHeight = 44.sp), color = colors.lampYellow, modifier = Modifier.padding(top = 14.dp).offset(y = 6.dp))
        Text(text, style = KompassTheme.typography.gloockRegular23.copy(fontSize = 26.sp, lineHeight = 31.sp), color = colors.boneWhite, modifier = Modifier.padding(bottom = 18.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.slateDivider))
    }
}

@Composable
private fun WhereItHappened(find: LocalFind, location: String, title: String, s: StoriesStrings, onOpenMap: (Double, Double, String) -> Unit) {
    val cleaned = cleanStoryLocation(location)
    val lat = find.latitude
    val lng = find.longitude
    // (0, 0) is how a missing coordinate arrives from the CMS. Never geocode a name.
    val hasPoint = lat != null && lng != null && !(lat == 0.0 && lng == 0.0)
    if (cleaned == null && !hasPoint) return
    val colors = KompassTheme.colors
    Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 34.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(s.whereItHappened, style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite, modifier = Modifier.semantics { heading() })
        cleaned?.let {
            Text(it, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal), color = KompassTheme.colors.white85)
        }
        if (hasPoint) {
            val name = storyPlaceName(location) ?: storyArea(location) ?: title
            StoryLocationRow(name, s.map, onMap = { onOpenMap(lat!!, lng!!, name) })
        }
    }
}
