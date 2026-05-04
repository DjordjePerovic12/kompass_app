package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.repository.AudioGuideRepository

class GetSignedAudioUrlUseCase(
    private val repository: AudioGuideRepository
) {
    suspend operator fun invoke(path: String): Result<String> = repository.getSignedAudioUrl(path)
}
