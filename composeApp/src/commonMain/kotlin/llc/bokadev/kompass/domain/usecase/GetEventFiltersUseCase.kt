package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.repository.EventRepository

class GetEventFiltersUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(): Result<List<EventFilter>> = repository.getEventFilters()
}
