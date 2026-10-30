package llc.bokadev.kompass.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.ui.theme.KompassTheme

/**
 * What the Nearby card's bottom-right image slot draws (cut-out spec B8). Only [Arch] and [None]
 * exist today: the `Cutout` mode depends on the `cutout_image_url`/`cutout_status` CMS fields,
 * which are [NEEDS APPROVAL] (spec B7), and is added once those ship.
 */
sealed interface SlotMode {
    data class Arch(val imageUrl: String) : SlotMode
    data object None : SlotMode
}

private val archShape = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50)

/**
 * Arch mode: the place's photo, crop-to-fill, clipped to an arch, with a hard `night` shadow made
 * of two stamps: lifted (−10, −6) and grounded (−10, 0), so no `lamp` gap shows under it.
 * Nothing is drawn until the photo has loaded (never a shadow around an empty arch), and a failed
 * load leaves the slot empty: the card is complete without an image. Decorative only; the card
 * title carries the name.
 */
@Composable
fun CutoutSlot(mode: SlotMode, modifier: Modifier = Modifier) {
    when (mode) {
        SlotMode.None -> Unit
        is SlotMode.Arch -> {
            val night = KompassTheme.colors.nightBlack
            val chalk = KompassTheme.colors.chalkWhite
            var loaded by remember(mode.imageUrl) { mutableStateOf(false) }
            Box(modifier) {
                if (loaded) {
                    Box(Modifier.fillMaxSize().offset((-10).dp, (-6).dp).background(night, archShape))
                    Box(Modifier.fillMaxSize().offset((-10).dp, 0.dp).background(night, archShape))
                }
                AsyncImage(
                    model = mode.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onSuccess = { loaded = true },
                    onError = { loaded = false },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(archShape)
                        // A chalk ground under the photo: a transparent PNG would otherwise show the
                        // night shadow through it and read as a solid black arch.
                        .then(if (loaded) Modifier.background(chalk) else Modifier)
                )
            }
        }
    }
}

@Preview
@Composable
private fun CutoutSlotPreview() {
    KompassTheme {
        Box(Modifier.size(width = 196.dp, height = 250.dp).background(KompassTheme.colors.lampYellow)) {
            CutoutSlot(SlotMode.Arch("https://example.com/cathedral.jpg"), Modifier.fillMaxSize())
        }
    }
}
