package llc.bokadev.kompass.core.util

interface OfflineAudioStore {
    fun cachedUri(cacheKey: String): String?

    /** Writes to a temporary file and renames it in place, so a half-written file never counts as cached. */
    fun persist(cacheKey: String, bytes: ByteArray): Result<String>

    /** Deletes the cached file. True if nothing remains afterwards. */
    fun remove(cacheKey: String): Boolean
}
