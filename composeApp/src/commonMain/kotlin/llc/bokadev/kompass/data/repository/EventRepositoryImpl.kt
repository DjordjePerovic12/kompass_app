package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.util.AppPreferences
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
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : EventRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getEvents(dateFilter: String, eventType: String): Result<List<Event>> = runCatching {
        val normalizedEventType = eventType.normalizedFilterValue()
        getActiveEventDtos()
            .map { it.toDomain() }
            .filter { event ->
                isUpcoming(event) &&
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
        getEventFilterDtos()
            .map { it.toDomain() }
    }

    override suspend fun getUpcomingEvents(): Result<List<Event>> = runCatching {
        getActiveEventDtos()
            .map { it.toDomain() }
            .filter(::isUpcoming)
    }

    override suspend fun getEventById(id: String): Result<Event> = runCatching {
        runCatching {
            supabase.from("events")
                .select { filter { eq("id", id) } }
                .decodeSingle<EventDto>()
        }.fold(
            onSuccess = { remoteEvent ->
                mergeEventIntoCache(remoteEvent)
                remoteEvent
            },
            onFailure = { remoteError ->
                readCachedEvents().firstOrNull { it.id == id } ?: throw remoteError
            }
        ).toDomain()
    }

    private suspend fun getActiveEventDtos(): List<EventDto> =
        runCatching {
            supabase.from("events")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "start_time", order = Order.ASCENDING)
                }
                .decodeList<EventDto>()
        }.fold(
            onSuccess = { remoteEvents ->
                cacheEvents(remoteEvents)
                remoteEvents
            },
            onFailure = { remoteError ->
                val cachedEvents = readCachedEvents()
                if (cachedEvents.isNotEmpty()) cachedEvents else throw remoteError
            }
        )

    private suspend fun getEventFilterDtos(): List<EventFilterDto> =
        runCatching {
            supabase.from("event_filters")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<EventFilterDto>()
        }.fold(
            onSuccess = { remoteFilters ->
                cacheEventFilters(remoteFilters)
                remoteFilters
            },
            onFailure = { remoteError ->
                val cachedFilters = readCachedEventFilters()
                if (cachedFilters.isNotEmpty()) cachedFilters else throw remoteError
            }
        )

    private fun cacheEvents(dtos: List<EventDto>) {
        appPreferences.setString(
            CACHE_KEY_ACTIVE_EVENTS,
            json.encodeToString(ListSerializer(EventDto.serializer()), dtos)
        )
    }

    private fun readCachedEvents(): List<EventDto> {
        val raw = appPreferences.getString(CACHE_KEY_ACTIVE_EVENTS) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(EventDto.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    private fun mergeEventIntoCache(dto: EventDto) {
        val merged = readCachedEvents()
            .filterNot { it.id == dto.id }
            .plus(dto)
            .sortedBy { it.startTime }
        cacheEvents(merged)
    }

    private fun cacheEventFilters(dtos: List<EventFilterDto>) {
        appPreferences.setString(
            CACHE_KEY_EVENT_FILTERS,
            json.encodeToString(ListSerializer(EventFilterDto.serializer()), dtos)
        )
    }

    private fun readCachedEventFilters(): List<EventFilterDto> {
        val raw = appPreferences.getString(CACHE_KEY_EVENT_FILTERS) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(EventFilterDto.serializer()), raw)
        }.getOrElse { emptyList() }
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

    private fun isUpcoming(event: Event): Boolean {
        val comparisonInstant = event.endTime?.let(::parseInstantOrNull)
            ?: parseInstantOrNull(event.startTime)
            ?: return true

        return comparisonInstant >= Clock.System.now()
    }

    private fun parseInstantOrNull(value: String): Instant? = runCatching {
        Instant.parse(value)
    }.getOrNull()

    private companion object {
        const val CACHE_KEY_ACTIVE_EVENTS = "offline_cache_active_events_v1"
        const val CACHE_KEY_EVENT_FILTERS = "offline_cache_event_filters_v1"
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
