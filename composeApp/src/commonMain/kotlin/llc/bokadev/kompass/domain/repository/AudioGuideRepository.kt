package llc.bokadev.kompass.domain.repository

interface AudioGuideRepository {
    suspend fun getSignedAudioUrl(path: String): Result<String>
}
