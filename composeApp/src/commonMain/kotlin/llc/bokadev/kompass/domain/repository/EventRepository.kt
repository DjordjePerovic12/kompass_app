package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.EventFilter

interface EventRepository {
    suspend fun getEvents(dateFilter: String = "all", eventType: String = "all"): Resource<List<Event>, NetworkError, String?>
    suspend fun getEventFilters(): Resource<List<EventFilter>, NetworkError, String?>
    suspend fun getUpcomingEvents(): Resource<List<Event>, NetworkError, String?>
    suspend fun getEventById(id: String): Resource<Event, NetworkError, String?>
}
