package llc.bokadev.kompass.presentation.localfinds

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_back
import kompass.composeapp.generated.resources.retry
import kompass.composeapp.generated.resources.stories_all
import kompass.composeapp.generated.resources.stories_audio_available
import kompass.composeapp.generated.resources.stories_empty_all
import kompass.composeapp.generated.resources.stories_empty_legends
import kompass.composeapp.generated.resources.stories_empty_localisms
import kompass.composeapp.generated.resources.stories_legend
import kompass.composeapp.generated.resources.stories_legends
import kompass.composeapp.generated.resources.stories_listen
import kompass.composeapp.generated.resources.stories_localism
import kompass.composeapp.generated.resources.stories_localisms
import kompass.composeapp.generated.resources.stories_min_read
import kompass.composeapp.generated.resources.stories_minute_read
import kompass.composeapp.generated.resources.stories_subtitle
import kompass.composeapp.generated.resources.stories_title
import llc.bokadev.kompass.core.utils.LocalAppLocale
import llc.bokadev.kompass.core.utils.buildPhotoUrl
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.presentation.localfinds.components.StoryCompactCard
import llc.bokadev.kompass.presentation.localfinds.components.StoryFeatureCard
import llc.bokadev.kompass.presentation.localfinds.components.StoryQuoteCard
import llc.bokadev.kompass.presentation.localfinds.utils.StoryCardLayout
import llc.bokadev.kompass.presentation.localfinds.utils.StoryKind
import llc.bokadev.kompass.presentation.shared.icons.ChevronLeftIcon
import llc.bokadev.kompass.presentation.shared.LamplightSegmentedControl
import llc.bokadev.kompass.presentation.shared.SegmentOption
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.pageGradientBackground
import org.jetbrains.compose.resources.stringResource

/**
 * Lamplight Legends & Localisms (stories spec Part 1): cultural reading, not a venue list. Card
 * shape follows content (feature / compact / quote); locations live on the story detail.
 */
@Composable
fun LocalFindsScreenContent(
    modifier: Modifier = Modifier,
    state: LocalFindsState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onFilterSelect: (StoryKind?) -> Unit,
    onStoryClick: (String) -> Unit
) {
    // Language from the composition (AppEnvironment), not Koin, so the content stays previewable.
    val lang = LocalAppLocale.current.substringBefore('-')
    val error = state.error
    LazyColumn(
        modifier = modifier.fillMaxSize().pageGradientBackground(),
        contentPadding = PaddingValues(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "top") { TopBar(state, onBackClick, onFilterSelect) }
        when {
            state.isLoading && state.cards.isEmpty() -> item(key = "skeleton") { Skeleton() }
            error != null && state.cards.isEmpty() -> item(key = "error") {
                Message(stringResource(error.messageRes), stringResource(Res.string.retry), onRetryClick)
            }
            state.cards.isEmpty() -> item(key = "empty") {
                Message(
                    when (state.filter) {
                        StoryKind.LEGEND -> stringResource(Res.string.stories_empty_legends)
                        StoryKind.LOCALISM -> stringResource(Res.string.stories_empty_localisms)
                        null -> stringResource(Res.string.stories_empty_all)
                    },
                    null
                ) {}
            }
        }
        items(state.cards, key = { it.id }) { card -> Card(card, lang, Modifier.padding(horizontal = 20.dp)) { onStoryClick(card.id) } }
    }
}

@Composable
private fun Card(card: StoryCard, lang: String, modifier: Modifier, onClick: () -> Unit) {
    val type = stringResource(if (card.kind == StoryKind.LOCALISM) Res.string.stories_localism else Res.string.stories_legend)
    val label = listOfNotNull(type, card.area).joinToString(" · ").uppercaseFor(lang)
    val readTime = card.readMinutes?.let { stringResource(Res.string.stories_min_read, it.toString()) }
    // No duration field in the CMS: "Listen" without a time rather than a guess.
    val listen = if (card.hasAudio) stringResource(Res.string.stories_listen) else null
    val description = listOfNotNull(
        type, card.title,
        card.readMinutes?.let { stringResource(Res.string.stories_minute_read, it.toString()) },
        if (card.hasAudio) stringResource(Res.string.stories_audio_available) else null
    ).joinToString(", ")
    when (card.layout) {
        StoryCardLayout.FEATURE -> StoryFeatureCard(label, card.title, card.excerpt, card.photo?.let(::buildPhotoUrl), readTime, listen, description, onClick, modifier)
        StoryCardLayout.COMPACT -> StoryCompactCard(label, card.title, card.excerpt, buildPhotoUrl(card.photo.orEmpty()), readTime, listen, description, onClick, modifier)
        StoryCardLayout.QUOTE -> StoryQuoteCard(label, card.title, card.excerpt, readTime, listen, description, onClick, modifier)
    }
}

@Composable
private fun TopBar(state: LocalFindsState, onBack: () -> Unit, onFilterSelect: (StoryKind?) -> Unit) {
    val colors = KompassTheme.colors
    val backLabel = stringResource(Res.string.detail_back)
    Box(Modifier.fillMaxWidth().background(colors.lampYellow)) {
        Text(
            "“",
            style = KompassTheme.typography.gloockRegular40.copy(fontSize = 150.sp, lineHeight = 150.sp),
            color = colors.nightBlack.copy(alpha = 0.14f),
            modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.statusBars).padding(end = 18.dp).offset(y = (-6).dp)
        )
        Column(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.nightBlack)
                    .clickable(onClick = onBack)
                    .semantics { role = Role.Button; contentDescription = backLabel },
                contentAlignment = Alignment.Center
            ) { ChevronLeftIcon(color = colors.lampYellow, size = 18.dp) }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(Res.string.stories_title),
                    style = KompassTheme.typography.gloockRegular40.copy(fontSize = 42.sp, lineHeight = 42.sp),
                    color = colors.nightBlack,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    stringResource(Res.string.stories_subtitle),
                    style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    color = colors.nightBlack
                )
            }
            // Only worth a control when there are two kinds to choose between.
            if (state.kinds.size > 1) {
                val options = listOf<StoryKind?>(null) + state.kinds
                val allLabel = stringResource(Res.string.stories_all)
                val legendsLabel = stringResource(Res.string.stories_legends)
                val localismsLabel = stringResource(Res.string.stories_localisms)
                LamplightSegmentedControl(
                    options = options.map { kind ->
                        SegmentOption(when (kind) { null -> allLabel; StoryKind.LEGEND -> legendsLabel; StoryKind.LOCALISM -> localismsLabel })
                    },
                    selectedIndex = options.indexOf(state.filter).coerceAtLeast(0),
                    onSelect = { onFilterSelect(options[it]) }
                )
            }
        }
    }
}

@Composable
private fun Skeleton() {
    val colors = KompassTheme.colors
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(380.dp).clip(RoundedCornerShape(24.dp)).background(colors.charcoal))
        repeat(2) { Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(22.dp)).background(colors.charcoal)) }
    }
}

@Composable
private fun Message(text: String, action: String?, onAction: () -> Unit) {
    val colors = KompassTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 23.sp), color = KompassTheme.colors.white85, textAlign = TextAlign.Center)
        if (action != null) {
            Text(
                action,
                style = KompassTheme.typography.geologicaMedium16.copy(fontWeight = FontWeight.SemiBold),
                color = colors.lampYellow,
                modifier = Modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onAction).padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Preview
@Composable
private fun LocalFindsScreenContentPreview() {
    KompassTheme {
        LocalFindsScreenContent(
            state = LocalFindsState(
                isLoading = false,
                kinds = listOf(StoryKind.LEGEND),
                cards = listOf(
                    StoryCard("alkima", StoryKind.LEGEND, StoryCardLayout.FEATURE, "Alkima, the Fairy Who Built Kotor", "Long before the first stone was laid, a wise fairy named Alkima warned the founders.", "Old Town Kotor", null, 3, true),
                    StoryCard("polako", StoryKind.LEGEND, StoryCardLayout.QUOTE, "Polako", "Slowly, take it easy.", null, null, 1, false)
                )
            ),
            onBackClick = {},
            onRetryClick = {},
            onFilterSelect = {},
            onStoryClick = {}
        )
    }
}
