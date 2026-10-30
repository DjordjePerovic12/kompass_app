package llc.bokadev.kompass.domain.repository

interface AudioGuideRepository {
    /** Legacy guide screens: downloads and caches the whole file, then returns the local URI. */
    suspend fun getSignedAudioUrl(path: String): Result<String>

    /** The downloaded file if there is one, else a streaming URL. Never downloads. */
    suspend fun streamUrl(path: String): Result<String>

    /**
     * Downloads [path] for offline listening. [onProgress] gets 0..1 when the size is known.
     * Succeeds only once the file is fully written and its size matches the server's.
     */
    suspend fun download(path: String, onProgress: (Float?) -> Unit): Result<Unit>

    fun removeDownload(path: String): Boolean

    /** True when [path] is already in the offline audio store, so it plays without a network. */
    fun isAvailableOffline(path: String): Boolean
}
