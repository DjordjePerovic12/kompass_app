package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.EventFilter
import llc.bokadev.kompass.domain.repository.EventRepository

class GetEventFiltersUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(): Resource<List<EventFilter>, NetworkError, String?> = repository.getEventFilters()
}
