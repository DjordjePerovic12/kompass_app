package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.mapCatching
import llc.bokadev.kompass.core.utils.networking.recover
import llc.bokadev.kompass.core.utils.networking.safeSupabaseCall
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.EventDto
import llc.bokadev.kompass.data.remote.dto.EventFilterDto
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.repository.EventRepository
import llc.bokadev.kompass.domain.model.EventDateFilter
import llc.bokadev.kompass.domain.model.isCurrentOrUpcoming
import llc.bokadev.kompass.domain.model.matches
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class EventRepositoryImpl(
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : EventRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getEvents(dateFilter: String, eventType: String): Resource<List<Event>, NetworkError, String?> {
        val normalizedEventType = eventType.normalizedFilterValue()
        val dateRule = EventDateFilter.fromKey(dateFilter)
        val now = Clock.System.now()
        return getActiveEventDtos().mapCatching { events ->
            events
                .map { it.toDomain() }
                .filter { event ->
                    event.isCurrentOrUpcoming(now) &&
                    event.matches(dateRule, now) &&
                    (normalizedEventType == "all" || event.category.normalizedFilterValue() == normalizedEventType)
                }
        }
    }

    override suspend fun getEventFilters(): Resource<List<EventFilter>, NetworkError, String?> =
        getEventFilterDtos()
            .mapCatching { filters -> filters.map { it.toDomain() } }

    override suspend fun getUpcomingEvents(): Resource<List<Event>, NetworkError, String?> =
        getActiveEventDtos().mapCatching { events ->
            events
                .map { it.toDomain() }
                .filter { it.isCurrentOrUpcoming(Clock.System.now()) }
        }

    override suspend fun getEventById(id: String): Resource<Event, NetworkError, String?> =
        safeSupabaseCall {
            supabase.from("events")
                .select { filter { eq("id", id) } }
                .decodeSingle<EventDto>()
                .also { remoteEvent -> mergeEventIntoCache(remoteEvent) }
        }.recover { _, _ ->
            readCachedEvents().firstOrNull { it.id == id }
        }.mapCatching { it.toDomain() }

    private suspend fun getActiveEventDtos(): Resource<List<EventDto>, NetworkError, String?> =
        safeSupabaseCall {
            supabase.from("events")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "start_time", order = Order.ASCENDING)
                }
                .decodeList<EventDto>()
                .also { remoteEvents -> cacheEvents(remoteEvents) }
        }.recover { _, _ ->
            readCachedEvents().ifEmpty { null }
        }

    private suspend fun getEventFilterDtos(): Resource<List<EventFilterDto>, NetworkError, String?> =
        safeSupabaseCall {
            supabase.from("event_filters")
                .select {
                    filter { eq("is_active", true) }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<EventFilterDto>()
                .also { remoteFilters -> cacheEventFilters(remoteFilters) }
        }.recover { _, _ ->
            readCachedEventFilters().ifEmpty { null }
        }

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

    private companion object {
        const val CACHE_KEY_ACTIVE_EVENTS = "offline_cache_active_events_v1"
        const val CACHE_KEY_EVENT_FILTERS = "offline_cache_event_filters_v1"
    }
}

private fun String.normalizedFilterValue(): String {
    val normalized = lowercase()
    return if (normalized.startsWith("all")) "all" else normalized
}
