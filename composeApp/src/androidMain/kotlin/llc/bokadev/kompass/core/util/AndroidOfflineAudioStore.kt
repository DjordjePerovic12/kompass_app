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
        val partial = File(target.parentFile, "$cacheKey.part")
        partial.writeBytes(bytes)
        check(partial.length() == bytes.size.toLong()) { "Audio could not be cached." }
        if (target.exists()) target.delete()
        check(partial.renameTo(target)) { "Audio could not be cached." }
        target.toURI().toString()
    }

    override fun remove(cacheKey: String): Boolean {
        val target = fileFor(cacheKey)
        return !target.exists() || target.delete()
    }

    private fun fileFor(cacheKey: String): File =
        File(File(context.filesDir, "audio-guides"), cacheKey)
}
