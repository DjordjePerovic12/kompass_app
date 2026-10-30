package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.utils.RedesignStrings
import llc.bokadev.kompass.core.utils.redesignStringsFor
import llc.bokadev.kompass.presentation.shared.PagerDots
import kotlin.math.absoluteValue

/** One Nearby page, already localized and formatted by the caller. */
data class NearbyCardUi(
    val id: String,
    val title: String,
    val meta: String,
    val imageUrl: String?,
    val hasAudio: Boolean,
    val isSaved: Boolean
)

/**
 * The Nearby pager: one [NearbyCard] at a time, proximity-ordered, swipe-driven, no auto-advance.
 * Neighbours peek in through the pager's content padding and shrink/fade with their distance
 * from the centre. [PagerDots] window long lists to 5 dots, and page changes are
 * announced as "Item n of total".
 */
@Composable
fun NearbyStackCarousel(
    items: List<NearbyCardUi>,
    strings: RedesignStrings,
    onOpen: (String) -> Unit,
    onPrimary: (NearbyCardUi) -> Unit,
    onToggleSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 26.dp),
            pageSpacing = 12.dp,
            key = { items[it].id },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    stateDescription = strings.carouselItemOf
                        .replace("{n}", (pagerState.currentPage + 1).toString())
                        .replace("{total}", items.size.toString())
                }
        ) { page ->
            val item = items[page]
            NearbyCard(
                title = item.title,
                meta = item.meta,
                imageUrl = item.imageUrl,
                hasAudio = item.hasAudio,
                isSaved = item.isSaved,
                strings = strings,
                onClick = { onOpen(item.id) },
                onPrimaryClick = { onPrimary(item) },
                onToggleSave = { onToggleSave(item.id) },
                modifier = Modifier.graphicsLayer {
                    val distance = pagerState.getOffsetDistanceInPages(page).absoluteValue.coerceIn(0f, 1f)
                    val scale = 1f - 0.06f * distance
                    scaleX = scale
                    scaleY = scale
                    alpha = 1f - 0.35f * distance
                }
            )
        }

        PagerDots(
            current = pagerState.currentPage,
            total = items.size,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Preview
@Composable
private fun NearbyStackCarouselPreview() {
    NearbyStackCarousel(
        items = listOf(
            NearbyCardUi("1", "Cathedral of Saint Tryphon", "Cathedral · 240 m", null, hasAudio = true, isSaved = false),
            NearbyCardUi("2", "Maritime Museum", "Museum · 410 m", null, hasAudio = false, isSaved = true)
        ),
        strings = redesignStringsFor("en"),
        onOpen = {},
        onPrimary = {},
        onToggleSave = {}
    )
}
