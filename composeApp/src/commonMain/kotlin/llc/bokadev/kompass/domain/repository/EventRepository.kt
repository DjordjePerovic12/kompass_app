package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter

interface EventRepository {
    suspend fun getEvents(dateFilter: String = "all", eventType: String = "all"): Result<List<Event>>
    suspend fun getEventFilters(): Result<List<EventFilter>>
    suspend fun getUpcomingEvents(): Result<List<Event>>
    suspend fun getEventById(id: String): Result<Event>
}
