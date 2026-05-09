package llc.bokadev.kompass.core.util

import android.content.Context
import java.io.File

class AndroidOfflineAudioStore(
    private val context: Context
) : OfflineAudioStore {

    override fun cachedUri(cacheKey: String): String? {
        val target = fileFor(cacheKey)
        return if (target.exists()) target.toURI().toString() else null
    }

    override fun persist(cacheKey: String, bytes: ByteArray): Result<String> = runCatching {
        val target = fileFor(cacheKey)
        target.parentFile?.mkdirs()
        target.writeBytes(bytes)
        target.toURI().toString()
    }

    private fun fileFor(cacheKey: String): File =
        File(File(context.filesDir, "audio-guides"), cacheKey)
}
