package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.repository.EventRepository

class GetEventByIdUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(id: String): Result<Event> = repository.getEventById(id)
}
