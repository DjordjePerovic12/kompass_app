package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.repository.EventRepository

class GetEventByIdUseCase(
    private val repository: EventRepository
) {
    suspend operator fun invoke(id: String): Resource<Event, NetworkError, String?> = repository.getEventById(id)
}
