@file:OptIn(ExperimentalTime::class)

package llc.bokadev.kompass.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The destination's clock. Event days, "today" and displayed times are Kotor-local, so a visitor
 * planning from another time zone sees the dates the venue will actually use.
 */
object Destination {
    const val TIME_ZONE_ID = "Europe/Podgorica"
    val timeZone: TimeZone = TimeZone.of(TIME_ZONE_ID)
}

/**
 * When an event is on, as parsed from the CMS.
 *
 * Rules for imperfect data (documented, tested, never displayed as facts):
 * - an unparseable start makes the event unschedulable: it is excluded from lists and filters;
 * - an unparseable end, or an end before the start, is treated as missing;
 * - a missing end means the event stays listed until the end of its start day in Kotor.
 *   That is a visibility rule only; no end time is ever shown for it.
 */
data class EventWindow(val start: Instant, val end: Instant?) {
    fun visibleUntil(zone: TimeZone = Destination.timeZone): Instant =
        end ?: start.toLocalDateTime(zone).date.plus(DatePeriod(days = 1)).atStartOfDayIn(zone)

    /** True when the event is on at some point in [[from], [untilExclusive]). */
    fun overlaps(from: Instant, untilExclusive: Instant, zone: TimeZone = Destination.timeZone): Boolean =
        start < untilExclusive && visibleUntil(zone) > from
}

fun Event.window(): EventWindow? {
    val start = startTime.toInstantOrNull() ?: return null
    val end = endTime?.toInstantOrNull()?.takeIf { it >= start }
    return EventWindow(start, end)
}

/** Ongoing or still to come at [now]. An event without a valid start is neither. */
fun Event.isCurrentOrUpcoming(now: Instant, zone: TimeZone = Destination.timeZone): Boolean =
    window()?.let { it.visibleUntil(zone) > now } ?: false

/** The date filters offered by the CMS's event_filters keys. */
enum class EventDateFilter { ALL, TODAY, THIS_WEEK, THIS_MONTH;
    companion object {
        /** "today", "this_week", "this_month"; anything starting with "all" or unknown is ALL. */
        fun fromKey(key: String): EventDateFilter = when (key.lowercase()) {
            "today" -> TODAY
            "this_week" -> THIS_WEEK
            "this_month" -> THIS_MONTH
            else -> ALL
        }
    }
}

/** The filter's period as destination-local midnights; null for ALL. Weeks start on Monday. */
fun EventDateFilter.period(now: Instant, zone: TimeZone = Destination.timeZone): Pair<Instant, Instant>? {
    val today = now.toLocalDateTime(zone).date
    val (start, endExclusive) = when (this) {
        EventDateFilter.ALL -> return null
        EventDateFilter.TODAY -> today to today.plus(DatePeriod(days = 1))
        EventDateFilter.THIS_WEEK -> today.plus(DatePeriod(days = -today.dayOfWeek.ordinal)).let { it to it.plus(DatePeriod(days = 7)) }
        EventDateFilter.THIS_MONTH -> LocalDate(today.year, today.month, 1).let { it to it.plus(DatePeriod(months = 1)) }
    }
    return start.atStartOfDayIn(zone) to endExclusive.atStartOfDayIn(zone)
}

/** Whether the event is on during the filter's period (overlap, so a festival that started yesterday is "today"). */
fun Event.matches(filter: EventDateFilter, now: Instant, zone: TimeZone = Destination.timeZone): Boolean {
    val window = window() ?: return false
    val (from, until) = filter.period(now, zone) ?: return true
    return window.overlaps(from, until, zone)
}

private fun String.toInstantOrNull(): Instant? = runCatching { Instant.parse(this) }.getOrNull()

/**
 * The Kotor day an event is listed under: its start day, or today when it started on an earlier
 * day and is still on (so a running festival sits under Today, not a past date). Null when unschedulable.
 */
fun Event.listingDay(now: Instant, zone: TimeZone = Destination.timeZone): LocalDate? {
    val window = window() ?: return null
    val startDay = window.start.toLocalDateTime(zone).date
    val today = now.toLocalDateTime(zone).date
    return if (startDay < today && window.visibleUntil(zone) > now) today else startDay
}

/** Started on an earlier Kotor day and still on at [now]. */
fun Event.isOngoingFromEarlierDay(now: Instant, zone: TimeZone = Destination.timeZone): Boolean {
    val window = window() ?: return false
    return window.start.toLocalDateTime(zone).date < now.toLocalDateTime(zone).date && window.visibleUntil(zone) > now
}

/** Chronological day groups (Kotor days), each sorted by start time. */
fun List<Event>.groupedByListingDay(now: Instant, zone: TimeZone = Destination.timeZone): List<Pair<LocalDate, List<Event>>> =
    mapNotNull { event -> event.listingDay(now, zone)?.let { it to event } }
        .groupBy({ it.first }, { it.second })
        .entries
        .sortedBy { it.key }
        .map { (day, events) -> day to events.sortedBy { it.window()?.start } }
