package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.repository.EventRepository

class GetUpcomingEventsUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(): Result<List<Event>> = repository.getUpcomingEvents()
}
