package llc.bokadev.kompass.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Shape and spacing tokens (Lamplight spec §1.3). */
object KompassDimens {
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

object KompassShapes {
    val cardL = RoundedCornerShape(KompassDimens.radiusCardL)
    val cardMustSee = RoundedCornerShape(KompassDimens.radiusCardMustSee)
    val cardEvent = RoundedCornerShape(KompassDimens.radiusCardEvent)
    val imageInner = RoundedCornerShape(KompassDimens.radiusImageInner)
    val pill = RoundedCornerShape(50)
    val searchField = RoundedCornerShape(KompassDimens.searchFieldRadius)
    val nav = RoundedCornerShape(KompassDimens.radiusNav)

    /** Deliberately square — notices read as operational, not as discovery (spec §1.3). */
    val notice = RoundedCornerShape(0.dp)
}
