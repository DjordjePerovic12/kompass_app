package llc.bokadev.kompass.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSTemporaryDirectory
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
class IosOfflineAudioStore : OfflineAudioStore {

    override fun cachedUri(cacheKey: String): String? {
        val path = filePath(cacheKey)
        return if (NSFileManager.defaultManager.fileExistsAtPath(path)) {
            NSURL.fileURLWithPath(path).absoluteString
        } else {
            null
        }
    }

    override fun persist(cacheKey: String, bytes: ByteArray): Result<String> = runCatching {
        val path = filePath(cacheKey)
        val file = fopen(path, "wb") ?: error("Audio could not be cached.")
        bytes.usePinned {
            val written = fwrite(it.addressOf(0), 1.convert(), bytes.size.convert(), file)
            check(written.toLong() == bytes.size.toLong()) { "Audio could not be cached." }
        }
        fclose(file)
        NSURL.fileURLWithPath(path).absoluteString ?: path
    }

    private fun filePath(cacheKey: String): String {
        val directory = cacheDirectoryPath()
        NSFileManager.defaultManager.createDirectoryAtPath(
            path = directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
        return "$directory/$cacheKey"
    }

    private fun cacheDirectoryPath(): String {
        val root = (NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
            .firstOrNull() as? String)
            ?: NSTemporaryDirectory()
        return "$root/audio-guides"
    }
}
