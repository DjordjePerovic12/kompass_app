package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.repository.EventRepository

class GetEventsUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(
        dateFilter: String = "all",
        eventType: String = "all"
    ): Resource<List<Event>, NetworkError, String?> = repository.getEvents(dateFilter = dateFilter, eventType = eventType)
}
