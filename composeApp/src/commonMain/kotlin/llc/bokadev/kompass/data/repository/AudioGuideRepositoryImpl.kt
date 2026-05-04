package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.minutes
import llc.bokadev.kompass.domain.repository.AudioGuideRepository

class AudioGuideRepositoryImpl(
    private val supabase: SupabaseClient
) : AudioGuideRepository {

    override suspend fun getSignedAudioUrl(path: String): Result<String> = runCatching {
        supabase.storage
            .from("audio")
            .createSignedUrl(path = path, expiresIn = 30.minutes)
    }
}
