package llc.bokadev.kompass.core.util

interface OfflineAudioStore {
    fun cachedUri(cacheKey: String): String?
    fun persist(cacheKey: String, bytes: ByteArray): Result<String>
}
