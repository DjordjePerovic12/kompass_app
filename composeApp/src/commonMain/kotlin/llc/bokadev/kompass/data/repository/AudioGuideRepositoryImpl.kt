package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlin.time.Duration.Companion.minutes
import llc.bokadev.kompass.BuildKonfig
import llc.bokadev.kompass.core.utils.OfflineAudioStore
import llc.bokadev.kompass.domain.repository.AudioGuideRepository

class AudioGuideRepositoryImpl(
    private val supabase: SupabaseClient,
    private val httpClient: HttpClient,
    private val offlineAudioStore: OfflineAudioStore
) : AudioGuideRepository {

    override suspend fun getSignedAudioUrl(path: String): Result<String> = runCatching {
        val normalizedPath = normalizeAudioPath(path)
        val cacheKey = buildCacheKey(normalizedPath)

        cachedUri(normalizedPath)?.let { cachedUri ->
            return@runCatching cachedUri
        }

        val remoteUrl = if (normalizedPath.startsWith("http://") || normalizedPath.startsWith("https://")) {
            normalizedPath
        } else {
            runCatching {
                supabase.storage
                    .from("audio")
                    .createSignedUrl(path = normalizedPath, expiresIn = 30.minutes)
            }.getOrElse {
                "${BuildKonfig.SUPABASE_URL}/storage/v1/object/public/audio/$normalizedPath"
            }
        }

        val downloadedBytes = runCatching { httpClient.get(remoteUrl).body<ByteArray>() }.getOrNull()
        if (downloadedBytes != null) {
            offlineAudioStore.persist(cacheKey, downloadedBytes).getOrNull()?.let { localUri ->
                return@runCatching localUri
            }
        }

        remoteUrl
    }

    override suspend fun streamUrl(path: String): Result<String> = runCatching {
        val normalizedPath = normalizeAudioPath(path)
        cachedUri(normalizedPath) ?: remoteUrl(normalizedPath)
    }

    override suspend fun download(path: String, onProgress: (Float?) -> Unit): Result<Unit> = runCatching {
        val normalizedPath = normalizeAudioPath(path)
        val url = remoteUrl(normalizedPath)
        var expected: Long? = null
        val response = httpClient.get(url) {
            onDownload { received, total ->
                expected = total?.takeIf { it > 0 }
                onProgress(expected?.let { (received.toFloat() / it).coerceIn(0f, 1f) })
            }
        }
        check(response.status.isSuccess()) { "Download failed (${response.status.value})." }
        val bytes = response.body<ByteArray>()
        check(bytes.isNotEmpty()) { "Download was empty." }
        expected?.let { check(bytes.size.toLong() == it) { "Download was incomplete." } }
        offlineAudioStore.persist(buildCacheKey(normalizedPath), bytes).getOrThrow()
        Unit
    }

    /** Removes the file under both the current and the legacy key; true if nothing remains. */
    override fun removeDownload(path: String): Boolean {
        val normalizedPath = normalizeAudioPath(path)
        return cacheKeys(normalizedPath).map { offlineAudioStore.remove(it) }.all { it }
    }

    private suspend fun remoteUrl(normalizedPath: String): String =
        if (normalizedPath.startsWith("http://") || normalizedPath.startsWith("https://")) {
            normalizedPath
        } else {
            runCatching {
                supabase.storage.from("audio").createSignedUrl(path = normalizedPath, expiresIn = 30.minutes)
            }.getOrElse {
                "${BuildKonfig.SUPABASE_URL}/storage/v1/object/public/audio/$normalizedPath"
            }
        }

    override fun isAvailableOffline(path: String): Boolean =
        cachedUri(normalizeAudioPath(path)) != null

    /** The current key first, then the pre-Oct-2026 leaf-name key, so earlier downloads still play. */
    private fun cachedUri(normalizedPath: String): String? =
        cacheKeys(normalizedPath).firstNotNullOfOrNull { offlineAudioStore.cachedUri(it) }

    private fun cacheKeys(normalizedPath: String): List<String> =
        listOf(buildCacheKey(normalizedPath), legacyCacheKey(normalizedPath)).distinct()

    private fun normalizeAudioPath(raw: String): String {
        val sanitized = raw.substringBefore("?").trim()

        return when {
            "/storage/v1/object/public/audio/" in sanitized ->
                sanitized.substringAfter("/storage/v1/object/public/audio/")
            "/storage/v1/object/sign/audio/" in sanitized ->
                sanitized.substringAfter("/storage/v1/object/sign/audio/")
            sanitized.startsWith("audio/") ->
                sanitized.removePrefix("audio/")
            sanitized.startsWith("http://") || sanitized.startsWith("https://") ->
                sanitized
            else ->
                sanitized.trimStart('/')
        }
    }

    /**
     * One file per *storage path*: "places/audio/fr/x.mp3" and "places/audio/en/x.mp3" (or two
     * buckets with the same file name) never share a download. URLs drop their scheme and host.
     */
    internal fun buildCacheKey(path: String): String = audioCacheKey(path)

    /** The old key: file name only. Read-only fallback for files downloaded before the fix. */
    private fun legacyCacheKey(path: String): String {
        val source = path.substringBefore("?")
        val extension = source.substringAfterLast('.', "").takeIf { it.isNotBlank() } ?: "mp3"
        val leaf = source.substringAfterLast('/').substringBeforeLast('.', "")
        val base = leaf
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .takeLast(72)
            .trim('_')
            .ifBlank { "audio_guide" }
        return "$base.$extension"
    }
}

/** Pure, for tests: `v2_` + the sanitized path; long paths keep their tail plus a hash of the whole. */
internal fun audioCacheKey(path: String): String {
    val source = path.substringBefore("?").substringAfter("://").let { p -> if ("://" in path) p.substringAfter('/') else p }
    val extension = source.substringAfterLast('.', "").takeIf { it.isNotBlank() && '/' !in it } ?: "mp3"
    val stem = source.substringBeforeLast('.', source).replace(Regex("[^A-Za-z0-9_-]"), "_").trim('_').ifBlank { "audio_guide" }
    val body = if (stem.length <= 96) stem else stem.takeLast(80) + "_" + stableHash(stem)
    return "v2_$body.$extension"
}

private fun stableHash(text: String): String {
    var h = 1125899906842597L
    text.forEach { h = 31 * h + it.code }
    return h.toULong().toString(16)
}
