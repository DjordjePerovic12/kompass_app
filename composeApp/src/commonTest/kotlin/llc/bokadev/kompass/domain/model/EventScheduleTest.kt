@file:OptIn(ExperimentalTime::class)

package llc.bokadev.kompass.domain.model

import kotlin.test.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class EventScheduleTest {

    // 7 Oct 2026, 12:00 in Kotor (CEST, UTC+2).
    private val noonInKotor = Instant.parse("2026-10-07T10:00:00Z")

    @Test fun multiDayEventThatStartedYesterdayIsToday() {
        val festival = event(start = "2026-10-06T16:00:00Z", end = "2026-10-09T20:00:00Z")
        assertTrue(festival.matches(EventDateFilter.TODAY, noonInKotor))
        assertTrue(festival.isCurrentOrUpcoming(noonInKotor))
    }

    @Test fun finishedEventIsNeitherCurrentNorToday() {
        val morning = event(start = "2026-10-07T06:00:00Z", end = "2026-10-07T08:00:00Z")
        assertFalse(morning.isCurrentOrUpcoming(noonInKotor))
    }

    @Test fun daysAreKotorDaysNotDeviceDays() {
        // 23:30 UTC on 7 Oct is 01:30 on 8 Oct in Kotor: tomorrow there, whatever the device says.
        val lateNight = event(start = "2026-10-07T23:30:00Z", end = null)
        assertFalse(lateNight.matches(EventDateFilter.TODAY, noonInKotor))
        assertTrue(lateNight.matches(EventDateFilter.THIS_WEEK, noonInKotor))
    }

    @Test fun missingEndStaysListedUntilTheEndOfItsKotorDay() {
        val concert = event(start = "2026-10-07T18:00:00Z", end = null) // 20:00 local
        assertTrue(concert.isCurrentOrUpcoming(Instant.parse("2026-10-07T21:00:00Z"))) // 23:00 local, still tonight
        assertFalse(concert.isCurrentOrUpcoming(Instant.parse("2026-10-07T22:00:00Z"))) // midnight local
        assertFalse(concert.matches(EventDateFilter.TODAY, Instant.parse("2026-10-08T10:00:00Z")))
    }

    @Test fun malformedTimestampsAreNeverClaimedCurrent() {
        val badStart = event(start = "next friday", end = "2026-10-09T20:00:00Z")
        assertNull(badStart.window())
        assertFalse(badStart.isCurrentOrUpcoming(noonInKotor))
        assertFalse(badStart.matches(EventDateFilter.ALL, noonInKotor))

        // A bad or backwards end is ignored, not trusted.
        val badEnd = event(start = "2026-10-07T18:00:00Z", end = "soon")
        assertEquals(null, badEnd.window()?.end)
        val backwards = event(start = "2026-10-07T18:00:00Z", end = "2026-10-01T18:00:00Z")
        assertEquals(null, backwards.window()?.end)
    }

    @Test fun weekStartsMondayAndMonthIsTheCalendarMonth() {
        val (weekStart, weekEnd) = EventDateFilter.THIS_WEEK.period(noonInKotor)!!
        assertEquals(Instant.parse("2026-10-04T22:00:00Z"), weekStart) // Mon 5 Oct 00:00 CEST
        assertEquals(Instant.parse("2026-10-11T22:00:00Z"), weekEnd)
        val (monthStart, _) = EventDateFilter.THIS_MONTH.period(noonInKotor)!!
        assertEquals(Instant.parse("2026-09-30T22:00:00Z"), monthStart)
        assertNull(EventDateFilter.ALL.period(noonInKotor))
    }

    @Test fun todayAcrossTheDaylightSavingChangeIs25Hours() {
        // Clocks go back on 25 Oct 2026 at 03:00 CEST → 02:00 CET.
        val (start, end) = EventDateFilter.TODAY.period(Instant.parse("2026-10-25T10:00:00Z"))!!
        assertEquals(Instant.parse("2026-10-24T22:00:00Z"), start)
        assertEquals(Instant.parse("2026-10-25T23:00:00Z"), end)
        // 23:30 CET on the 25th is still that day.
        assertTrue(event(start = "2026-10-25T22:30:00Z", end = null).matches(EventDateFilter.TODAY, Instant.parse("2026-10-25T10:00:00Z")))
    }

    @Test fun filterKeysFromTheCms() {
        assertEquals(EventDateFilter.TODAY, EventDateFilter.fromKey("today"))
        assertEquals(EventDateFilter.THIS_WEEK, EventDateFilter.fromKey("THIS_WEEK"))
        assertEquals(EventDateFilter.ALL, EventDateFilter.fromKey("all_dates"))
        assertEquals(EventDateFilter.ALL, EventDateFilter.fromKey("unknown"))
    }

    private fun event(start: String, end: String?) = Event(
        id = "e", name = mapOf("en" to "e"), description = emptyMap(), venue = emptyMap(),
        latitude = 0.0, longitude = 0.0, category = "music", startTime = start, endTime = end,
        price = null, ticketUrl = null, isRecurring = false, photos = emptyList()
    )
}

class EventGroupingTest {
    private val noon = Instant.parse("2026-10-07T10:00:00Z")

    @Test fun runningFestivalIsListedUnderTodayAndFlaggedOngoing() {
        val festival = event("f", "2026-10-05T16:00:00Z", "2026-10-09T20:00:00Z")
        val tonight = event("t", "2026-10-07T18:00:00Z", null)
        val friday = event("x", "2026-10-09T18:00:00Z", null)
        val groups = listOf(friday, tonight, festival).groupedByListingDay(noon)
        assertEquals(listOf("2026-10-07", "2026-10-09"), groups.map { it.first.toString() })
        assertEquals(listOf("f", "t"), groups.first().second.map { it.id })
        assertTrue(festival.isOngoingFromEarlierDay(noon))
        assertFalse(tonight.isOngoingFromEarlierDay(noon))
    }

    private fun event(id: String, start: String, end: String?) = Event(
        id = id, name = mapOf("en" to id), description = emptyMap(), venue = emptyMap(),
        latitude = 0.0, longitude = 0.0, category = "music", startTime = start, endTime = end,
        price = null, ticketUrl = null, isRecurring = false, photos = emptyList()
    )
}
