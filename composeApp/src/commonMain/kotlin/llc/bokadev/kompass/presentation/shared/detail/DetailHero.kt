package llc.bokadev.kompass.presentation.shared.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.shared.icons.BookmarkIcon
import llc.bokadev.kompass.presentation.shared.icons.ChevronLeftIcon
import llc.bokadev.kompass.presentation.shared.PagerDots
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.presentation.shared.icons.DetailGlyph
import llc.bokadev.kompass.presentation.shared.icons.DetailIcon
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_photo_of
import org.jetbrains.compose.resources.stringResource

/**
 * Full-bleed photo carousel at the top of the place detail screen (spec §1.2): swipeable ordered
 * CMS photos (no auto-advance), dark scrims top and bottom, Back and Save overlay buttons, and
 * the shared [PagerDots]. With no photos it shrinks to a short block with a landmark glyph
 * instead of a full-height empty colour. [parallax] supplies the page scroll in px for the
 * 0.5× hero parallax (0 with reduced motion).
 */
@Composable
fun DetailHero(
    photoUrls: List<String>,
    height: Dp,
    isSaved: Boolean,
    backLabel: String,
    saveLabel: String,
    savedStateLabel: String,
    onBack: () -> Unit,
    /** Null hides the save button (e.g. events, which aren't saveable). */
    onToggleSave: (() -> Unit)?,
    modifier: Modifier = Modifier,
    parallax: () -> Float = { 0f }
) {
    val colors = KompassTheme.colors
    val heroHeight = if (photoUrls.isEmpty()) 200.dp else height
    val pagerState = rememberPagerState(pageCount = { photoUrls.size })
    val photoOfLabel = stringResource(Res.string.detail_photo_of, (pagerState.currentPage + 1).toString(), photoUrls.size.toString())

    Box(modifier = modifier.fillMaxWidth().height(heroHeight).clip(RectangleShape)) {
        if (photoUrls.isEmpty()) {
            MissingPhoto()
        } else {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = photoUrls.size > 1,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = parallax() * 0.5f }
                    .semantics {
                        if (photoUrls.size > 1) {
                            liveRegion = LiveRegionMode.Polite
                            stateDescription = photoOfLabel
                        }
                    }
            ) { page -> HeroPhoto(photoUrls[page]) }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Brush.verticalGradient(listOf(colors.nightBlack.copy(alpha = 0.7f), Color.Transparent)))
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
                // A soft shade for the page dots; the page behind is a gradient, not flat night,
                // so the photo no longer fades into a solid colour.
                .background(Brush.verticalGradient(listOf(Color.Transparent, colors.nightBlack.copy(alpha = 0.45f))))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            OverlayButton(description = backLabel, onClick = onBack) {
                ChevronLeftIcon(color = colors.chalkWhite, size = 18.dp)
            }
            Spacer(Modifier.weight(1f))
            if (onToggleSave != null) OverlayButton(description = saveLabel, stateLabel = savedStateLabel, onClick = onToggleSave) {
                BookmarkIcon(filled = isSaved, color = colors.chalkWhite, size = 18.dp)
            }
        }

        PagerDots(
            current = pagerState.currentPage,
            total = photoUrls.size,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp)
        )
    }
}

@Composable
private fun HeroPhoto(url: String) {
    var failed by remember(url) { mutableStateOf(false) }
    if (failed) {
        MissingPhoto()
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = { failed = true },
            modifier = Modifier.fillMaxSize().background(KompassTheme.colors.slateDark)
        )
    }
}

@Composable
private fun MissingPhoto() {
    val colors = KompassTheme.colors
    Box(Modifier.fillMaxSize().background(colors.slateDark), contentAlignment = Alignment.Center) {
        DetailIcon(DetailGlyph.Landmark, color = colors.pebbleGray, size = 32.dp)
    }
}

@Composable
private fun OverlayButton(
    description: String,
    onClick: () -> Unit,
    stateLabel: String? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(KompassTheme.colors.nightBlack.copy(alpha = 0.62f))
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = description
                if (stateLabel != null) stateDescription = stateLabel
            },
        contentAlignment = Alignment.Center
    ) { content() }
}

@Preview
@Composable
private fun DetailHeroPreview() {
    DetailHero(
        photoUrls = emptyList(),
        height = 480.dp,
        isSaved = false,
        backLabel = "Back",
        saveLabel = "Save Cathedral of Saint Tryphon",
        savedStateLabel = "Not saved",
        onBack = {},
        onToggleSave = {}
    )
}
