package llc.bokadev.kompass.presentation.screens.home

import llc.bokadev.kompass.core.util.AppStrings
import llc.bokadev.kompass.core.util.localizedTypeLabel
import llc.bokadev.kompass.core.util.toTitleCaseLabel
import llc.bokadev.kompass.core.util.toClockTime
import llc.bokadev.kompass.core.util.toNoticeDate
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.presentation.screens.essentials.formatDistance
import llc.bokadev.kompass.presentation.screens.home.components.NoticeLevel
import kotlin.math.roundToInt

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
fun Event.toRecurrenceSuffix(strings: AppStrings): String =
    if (isRecurring) " · ${strings.redesign.eventRecurringWeekly}" else ""

/**
 * The Must See card's area label (spec §3.3): the matched area's own short name, or
 * "Worth the Drive" when the place's zone doesn't match any of the three curated areas —
 * derived only from the place's own `zone` field, never a live-location distance (Must See
 * items have no GPS context, unlike Nearby's `NearbyPlace.distanceKm`). Null when the place has
 * no zone at all: the label is hidden rather than guessed.
 */
fun Place.toMustSeeAreaLabel(strings: AppStrings): String? {
    if (zone.isNullOrBlank()) return null
    val matchedArea = HomeArea.entries
        .filter { it != HomeArea.DEVICE }
        .firstOrNull { it.matchesZone(zone) }
    return matchedArea?.shortLabel(strings) ?: strings.redesign.worthDrive
}

fun InfoNotice.toNoticeLevel(): NoticeLevel = when (priorityRank()) {
    0 -> NoticeLevel.URGENT
    1 -> NoticeLevel.IMPORTANT
    else -> NoticeLevel.GENERAL
}

/** "Important · until 12. Oct" (or just "Important" with no end date) — never fabricated, hides the "until" segment when `endsAt` is absent. */
fun InfoNotice.toNoticeLabel(lang: String, strings: AppStrings): String {
    val levelText = when (toNoticeLevel()) {
        NoticeLevel.URGENT -> strings.redesign.noticeUrgent
        NoticeLevel.IMPORTANT -> strings.redesign.noticeImportant
        NoticeLevel.GENERAL -> strings.redesign.noticeGeneral
    }
    val untilDate = endsAt?.toNoticeDate(lang)
    return if (untilDate != null) "$levelText · ${strings.redesign.noticeUntil} $untilDate" else levelText
}

/**
 * The Nearby card's meta line: "Cathedral · 240 m" (straight-line, never a walking time) with live location, else "Cathedral · Old Town"
 * from the place's own zone. Either half is dropped when absent, never invented.
 */
fun NearbyPlace.toNearbyMeta(showDistance: Boolean, strings: AppStrings): String {
    val typeLabel = place.localizedTypeLabel(strings)
    val locationPart = if (showDistance) {
        (distanceKm * 1000).roundToInt().formatDistance()
    } else {
        place.zone?.toTitleCaseLabel()
    }
    return listOfNotNull(typeLabel, locationPart?.takeIf { it.isNotBlank() }).joinToString(" · ")
}
