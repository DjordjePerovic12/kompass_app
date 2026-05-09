package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlin.time.Duration.Companion.minutes
import llc.bokadev.kompass.BuildKonfig
import llc.bokadev.kompass.core.util.OfflineAudioStore
import llc.bokadev.kompass.domain.repository.AudioGuideRepository

class AudioGuideRepositoryImpl(
    private val supabase: SupabaseClient,
    private val httpClient: HttpClient,
    private val offlineAudioStore: OfflineAudioStore
) : AudioGuideRepository {

    override suspend fun getSignedAudioUrl(path: String): Result<String> = runCatching {
        val normalizedPath = normalizeAudioPath(path)
        val cacheKey = buildCacheKey(normalizedPath)

        offlineAudioStore.cachedUri(cacheKey)?.let { cachedUri ->
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

    private fun buildCacheKey(path: String): String {
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
