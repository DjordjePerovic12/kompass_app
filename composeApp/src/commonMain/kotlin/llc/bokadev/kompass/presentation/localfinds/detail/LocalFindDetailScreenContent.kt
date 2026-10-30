package llc.bokadev.kompass.presentation.localfinds.detail

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.stories_legend
import kompass.composeapp.generated.resources.stories_listen_to_story
import kompass.composeapp.generated.resources.stories_listen_to_story_duration
import kompass.composeapp.generated.resources.stories_loading_story
import kompass.composeapp.generated.resources.stories_localism
import kompass.composeapp.generated.resources.stories_map
import kompass.composeapp.generated.resources.stories_min_read
import kompass.composeapp.generated.resources.stories_next_story
import kompass.composeapp.generated.resources.stories_pause_story
import kompass.composeapp.generated.resources.stories_story_region
import kompass.composeapp.generated.resources.stories_where_it_happened
import llc.bokadev.kompass.core.audio.NavAudioMode
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.toClockDuration
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.LocalFind
import llc.bokadev.kompass.presentation.localfinds.components.DropCapParagraph
import llc.bokadev.kompass.presentation.localfinds.components.NextStoryCard
import llc.bokadev.kompass.presentation.localfinds.components.StoryLocationRow
import llc.bokadev.kompass.presentation.localfinds.utils.StoryKind
import llc.bokadev.kompass.presentation.localfinds.utils.cleanStoryLocation
import llc.bokadev.kompass.presentation.localfinds.utils.pullQuoteOf
import llc.bokadev.kompass.presentation.localfinds.utils.readMinutes
import llc.bokadev.kompass.presentation.localfinds.utils.storyArea
import llc.bokadev.kompass.presentation.localfinds.utils.storyKind
import llc.bokadev.kompass.presentation.localfinds.utils.storyParagraphs
import llc.bokadev.kompass.presentation.localfinds.utils.storyPlaceName
import llc.bokadev.kompass.presentation.shared.icons.EssentialsGlyph
import llc.bokadev.kompass.presentation.shared.icons.EssentialsIcon
import llc.bokadev.kompass.presentation.shared.icons.ChevronLeftIcon
import llc.bokadev.kompass.presentation.shared.detail.DetailCompactBar
import llc.bokadev.kompass.presentation.search.components.SearchGlyph
import llc.bokadev.kompass.presentation.search.components.SearchGlyphIcon
import llc.bokadev.kompass.presentation.shared.ListenPill
import llc.bokadev.kompass.presentation.shared.StatusBarIconsEffect
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import llc.bokadev.kompass.presentation.shared.toDisc
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource

/**
 * Lamplight story detail (stories spec Part 2): reading first. Hero (or a compact `lamp` header),
 * title block, the body with a drop cap, where it happened, next story. No save, no map embed.
 * Loading and failure (nothing to show yet) are BaseContentView's slots in the Screen.
 */
@Composable
fun LocalFindDetailScreenContent(
    modifier: Modifier = Modifier,
    state: LocalFindDetailState,
    /** The center button's mode for this story's track; null without audio. */
    audioMode: NavAudioMode?,
    onBackClick: () -> Unit,
    onListenClick: () -> Unit,
    onMapClick: (lat: Double, lng: Double, title: String) -> Unit,
    onNextStoryClick: (String) -> Unit,
    onUserScrolled: () -> Unit
) {
    val find = state.find ?: return
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val colors = KompassTheme.colors
    val density = LocalDensity.current
    val reduceMotion = rememberReduceMotion()
    val scroll = rememberScrollState()
    val backLabel = stringResource(Res.string.detail_back)
    val storyRegion = stringResource(Res.string.stories_story_region)
    val title = find.localizedTitle(lang)
    val body = find.localizedBody(lang)
    val paragraphs = remember(body) { storyParagraphs(body) }
    val location = find.localizedLocation(lang)
    val photo = find.photos.firstOrNull { it.isNotBlank() }
    val heroHeight = 380.dp
    val compactVisible by remember(photo) {
        derivedStateOf { scroll.value > with(density) { (if (photo != null) heroHeight - 80.dp else 120.dp).toPx() } }
    }
    LaunchedEffect(scroll.isScrollInProgress) { if (scroll.isScrollInProgress) onUserScrolled() }
    StatusBarIconsEffect(lightIcons = photo != null || compactVisible)

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            if (photo != null) Hero(buildPhotoUrl(photo), heroHeight, backLabel, onBackClick)
            else CompactHeader(backLabel, onBackClick)

            TitleBlock(
                find = find,
                title = title,
                readMinutes = readMinutes(body),
                placeName = storyPlaceName(location),
                audioMode = audioMode,
                lang = lang,
                reduceMotion = reduceMotion,
                onListen = onListenClick,
                // The block overlaps the hero by 24dp (spec §2.1); without a photo it sits below the header.
                modifier = if (photo != null) Modifier.overlapUp(24.dp) else Modifier.padding(top = 20.dp)
            )

            Column(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 22.dp).semantics { paneTitle = storyRegion },
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                if (paragraphs.isEmpty() && audioMode != null) {
                    // Only a title + audio: the listen block is the story.
                    Text(stringResource(Res.string.stories_listen_to_story), style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite)
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

            WhereItHappened(find, location, title, onMapClick)

            state.next?.let {
                NextStoryCard(
                    label = stringResource(Res.string.stories_next_story).uppercaseFor(lang),
                    title = it.title,
                    onClick = { onNextStoryClick(it.id) },
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 30.dp)
                )
            }
            // The shell already insets content above the floating bar; this is breathing room.
            Spacer(Modifier.height(26.dp))
        }

        AnimatedVisibility(visible = compactVisible, enter = fadeIn(), exit = fadeOut()) {
            Column {
                DetailCompactBar(title = title, backLabel = backLabel, onBack = onBackClick)
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
private fun Modifier.overlapUp(by: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val shift = by.roundToPx()
    layout(placeable.width, (placeable.height - shift).coerceAtLeast(0)) { placeable.place(0, -shift) }
}

@Composable
private fun Hero(url: String, height: Dp, backLabel: String, onBack: () -> Unit) {
    val colors = KompassTheme.colors
    Box(Modifier.fillMaxWidth().height(height).background(colors.slateDark)) {
        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxWidth().height(120.dp).background(Brush.verticalGradient(listOf(colors.black.copy(alpha = 0.45f), Color.Transparent))))
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
    lang: String,
    reduceMotion: Boolean,
    onListen: () -> Unit,
    modifier: Modifier
) {
    val colors = KompassTheme.colors
    val type = stringResource(if (find.storyKind == StoryKind.LOCALISM) Res.string.stories_localism else Res.string.stories_legend)
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
                        Text(stringResource(Res.string.stories_min_read, it.toString()), style = KompassTheme.typography.geologicaRegular13, color = KompassTheme.colors.white85)
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
            val pillLabel = duration?.let { stringResource(Res.string.stories_listen_to_story_duration, it) }
                ?: stringResource(Res.string.stories_listen_to_story)
            val pauseLabel = stringResource(Res.string.stories_pause_story)
            val loadingLabel = stringResource(Res.string.stories_loading_story)
            ListenPill(
                label = pillLabel,
                disc = mode.toDisc(),
                unavailable = mode is NavAudioMode.Unavailable,
                description = when (mode) {
                    is NavAudioMode.Playing -> pauseLabel
                    is NavAudioMode.Loading -> loadingLabel
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
private fun WhereItHappened(find: LocalFind, location: String, title: String, onOpenMap: (Double, Double, String) -> Unit) {
    val cleaned = cleanStoryLocation(location)
    val lat = find.latitude
    val lng = find.longitude
    // (0, 0) is how a missing coordinate arrives from the CMS. Never geocode a name.
    val hasPoint = lat != null && lng != null && !(lat == 0.0 && lng == 0.0)
    if (cleaned == null && !hasPoint) return
    val colors = KompassTheme.colors
    Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 34.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.stories_where_it_happened), style = KompassTheme.typography.gloockRegular26, color = colors.boneWhite, modifier = Modifier.semantics { heading() })
        cleaned?.let {
            Text(it, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal), color = KompassTheme.colors.white85)
        }
        if (hasPoint) {
            val name = storyPlaceName(location) ?: storyArea(location) ?: title
            StoryLocationRow(name, stringResource(Res.string.stories_map), onMap = { onOpenMap(lat!!, lng!!, name) })
        }
    }
}

@Preview
@Composable
private fun LocalFindDetailScreenContentPreview() {
    KompassTheme {
        LocalFindDetailScreenContent(
            modifier = Modifier.pageGradientBackground(),
            state = LocalFindDetailState(
                isLoading = false,
                find = LocalFind(
                    id = "alkima",
                    cityId = "kotor",
                    title = mapOf("en" to "Alkima, the Fairy Who Built Kotor"),
                    body = mapOf("en" to "Long before the first stone was laid, a wise fairy named Alkima warned the founders.\n\n> They did not listen.\n\nThe city rose anyway."),
                    audioFile = null,
                    location = mapOf("en" to "Old Town Kotor — Karampana Wall"),
                    latitude = 42.42,
                    longitude = 18.77,
                    photos = emptyList(),
                    isActive = true,
                    sortOrder = 0
                ),
                next = NextStory("rocks", "The stones of Our Lady of the Rocks")
            ),
            audioMode = null,
            onBackClick = {},
            onListenClick = {},
            onMapClick = { _, _, _ -> },
            onNextStoryClick = {},
            onUserScrolled = {}
        )
    }
}
