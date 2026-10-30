package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.repository.AudioGuideRepository

class GetSignedAudioUrlUseCase(
    private val repository: AudioGuideRepository
) {
    suspend operator fun invoke(path: String): Resource<String, NetworkError, String?> = repository.getSignedAudioUrl(path)
}
