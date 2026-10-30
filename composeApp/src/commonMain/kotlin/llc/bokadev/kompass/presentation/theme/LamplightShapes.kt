package llc.bokadev.kompass.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** "Lamplight" shape and spacing tokens (spec §1.3). */
object LamplightDimens {
    val screenPadding = 20.dp
    val sectionTopSpacing = 32.dp // midpoint of the spec's 30-34dp range

    val radiusCardL = 26.dp // Nearby card
    val radiusCardMustSee = 22.dp
    val radiusCardEvent = 20.dp
    val radiusImageInner = 14.dp // event image
    val searchFieldRadius = 24.dp
    val radiusNav = 28.dp

    val minTouchTarget = 48.dp
}

object LamplightShapes {
    val cardL = RoundedCornerShape(LamplightDimens.radiusCardL)
    val cardMustSee = RoundedCornerShape(LamplightDimens.radiusCardMustSee)
    val cardEvent = RoundedCornerShape(LamplightDimens.radiusCardEvent)
    val imageInner = RoundedCornerShape(LamplightDimens.radiusImageInner)
    val pill = RoundedCornerShape(50)
    val searchField = RoundedCornerShape(LamplightDimens.searchFieldRadius)
    val nav = RoundedCornerShape(LamplightDimens.radiusNav)

    /** Deliberately square — notices read as operational, not as discovery (spec §1.3). */
    val notice = RoundedCornerShape(0.dp)
}
