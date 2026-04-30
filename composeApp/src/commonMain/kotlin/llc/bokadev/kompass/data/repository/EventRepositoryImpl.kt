package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.EventDto
import llc.bokadev.kompass.data.remote.dto.EventFilterDto
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.repository.EventRepository
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class EventRepositoryImpl(
    private val supabase: SupabaseClient
) : EventRepository {

    override suspend fun getEvents(dateFilter: String, eventType: String): Result<List<Event>> = runCatching {
        val normalizedEventType = eventType.normalizedFilterValue()
        supabase.from("events")
            .select {
                filter { eq("is_active", true) }
                order(column = "start_time", order = Order.ASCENDING)
            }
            .decodeList<EventDto>()
            .map { it.toDomain() }
            .filter { event ->
                matchesDateFilter(event, dateFilter) &&
                (normalizedEventType == "all" || event.category.normalizedFilterValue() == normalizedEventType)
            }
    }

    private fun matchesDateFilter(event: Event, dateKey: String): Boolean {
        val range = eventRangeFor(dateKey) ?: return true
        return runCatching {
            val zone = TimeZone.currentSystemDefault()
            val eventDate = Instant.parse(event.startTime).toLocalDateTime(zone).date
            eventDate >= range.start && eventDate < range.endExclusive
        }.getOrDefault(true)
    }

    override suspend fun getEventFilters(): Result<List<EventFilter>> = runCatching {
        supabase.from("event_filters")
            .select {
                filter { eq("is_active", true) }
                order(column = "sort_order", order = Order.ASCENDING)
            }
            .decodeList<EventFilterDto>()
            .map { it.toDomain() }
    }

    override suspend fun getUpcomingEvents(): Result<List<Event>> = runCatching {
        supabase.from("events")
            .select {
                filter { eq("is_active", true) }
                order(column = "start_time", order = Order.ASCENDING)
            }
            .decodeList<EventDto>()
            .map { it.toDomain() }
    }

    override suspend fun getEventById(id: String): Result<Event> = runCatching {
        supabase.from("events")
            .select { filter { eq("id", id) } }
            .decodeSingle<EventDto>()
            .toDomain()
    }

    private fun eventRangeFor(dateFilter: String): EventDateRange? {
        val normalizedDateFilter = dateFilter.normalizedFilterValue()
        if (normalizedDateFilter == "all") return null

        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date

        val start: LocalDate = when (normalizedDateFilter) {
            "today" -> today
            "this_week" -> today.plus(DatePeriod(days = -today.dayOfWeek.ordinal))
            "this_month" -> LocalDate(today.year, today.month, 1)
            else -> return null
        }

        val end: LocalDate = when (normalizedDateFilter) {
            "today" -> start.plus(DatePeriod(days = 1))
            "this_week" -> start.plus(DatePeriod(days = 7))
            "this_month" -> start.plus(DatePeriod(months = 1))
            else -> return null
        }

        return EventDateRange(start = start, endExclusive = end)
    }
}

private data class EventDateRange(
    val start: LocalDate,
    val endExclusive: LocalDate
)

private fun String.normalizedFilterValue(): String {
    val normalized = lowercase()
    return if (normalized.startsWith("all")) "all" else normalized
}
