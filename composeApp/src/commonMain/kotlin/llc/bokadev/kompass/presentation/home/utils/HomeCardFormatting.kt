package llc.bokadev.kompass.presentation.home.utils

import androidx.compose.runtime.Composable
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.redesign_worth_drive
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.core.utils.toClockTime
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.shared.formatDistance
import llc.bokadev.kompass.presentation.shared.localizedTypeLabel
import llc.bokadev.kompass.presentation.shared.shortLabel
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import llc.bokadev.kompass.domain.model.HomeArea

/** "HH:MM – HH:MM", or just the start time if there's no end. */
fun Event.toHomeEventMeta(): String {
    val start = startTime.toClockTime()
    val end = endTime?.toClockTime()
    return when {
        start != null && end != null -> "$start – $end"
        start != null -> start
        else -> ""
    }
}

/** " · Weekly" when the data actually says this event recurs — never invented. */
fun Event.toRecurrenceSuffix(weeklyLabel: String): String =
    if (isRecurring) " · $weeklyLabel" else ""

/**
 * The Must See card's area label (spec §3.3): the matched area's own short name, or
 * "Worth the Drive" when the place's zone doesn't match any of the three curated areas —
 * derived only from the place's own `zone` field, never a live-location distance (Must See
 * items have no GPS context, unlike Nearby's `NearbyPlace.distanceKm`). Null when the place has
 * no zone at all: the label is hidden rather than guessed.
 */
@Composable
fun Place.toMustSeeAreaLabel(): String? {
    if (zone.isNullOrBlank()) return null
    val matchedArea = HomeArea.entries
        .filter { it != HomeArea.DEVICE }
        .firstOrNull { it.matchesZone(zone) }
    return matchedArea?.shortLabel() ?: stringResource(Res.string.redesign_worth_drive)
}

/**
 * The Nearby card's meta line: "Cathedral · 240 m" (straight-line, never a walking time) with live location, else "Cathedral · Old Town"
 * from the place's own zone. Either half is dropped when absent, never invented.
 */
@Composable
fun NearbyPlace.toNearbyMeta(showDistance: Boolean): String {
    val typeLabel = place.localizedTypeLabel()
    val locationPart = if (showDistance) {
        (distanceKm * 1000).roundToInt().formatDistance()
    } else {
        place.zone?.toTitleCaseLabel()
    }
    return listOfNotNull(typeLabel, locationPart?.takeIf { it.isNotBlank() }).joinToString(" · ")
}
