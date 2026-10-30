package llc.bokadev.kompass.domain.model

/** What a free audio track belongs to; part of the track's identity and its navigation target. */
enum class AudioSource { PLACE, ACTIVITY, STORY }

/**
 * The file chosen for the app language: the language's own track, else the English one
 * ([isEnglishFallback] = true, shown as "English audio"). Never claims a language it doesn't have.
 */
data class AudioChoice(val path: String, val lang: String, val isEnglishFallback: Boolean)

/** The one language rule for every audio source. Blank paths count as missing. */
fun chooseAudio(files: Map<String, String>?, lang: String): AudioChoice? {
    val usable = files?.filterValues { it.isNotBlank() }.orEmpty()
    usable[lang]?.let { return AudioChoice(it.trim(), lang, isEnglishFallback = false) }
    usable["en"]?.let { return AudioChoice(it.trim(), "en", isEnglishFallback = lang != "en") }
    return null
}

/**
 * One item of the free audio catalog: a place, activity or story whose regular `audio_file` has a
 * playable language (DEEP audio is not part of the pilot catalog). Localized fields are raw maps;
 * the screen picks the language.
 */
data class AudioCatalogEntry(
    val source: AudioSource,
    val id: String,
    val title: Map<String, String>,
    /** Area text: place zone, activity/story location. */
    val area: Map<String, String>?,
    val zone: String?,
    val latitude: Double?,
    val longitude: Double?,
    val files: Map<String, String>,
    val photo: String?,
    /** Place CMS `sub_category` (e.g. "cathedral"); null for activities and stories. */
    val subtype: String? = null,
    val sortOrder: Int
) {
    fun localizedTitle(lang: String): String = title[lang]?.takeIf { it.isNotBlank() } ?: title["en"].orEmpty()
    fun localizedArea(lang: String): String = area?.get(lang)?.takeIf { it.isNotBlank() } ?: area?.get("en").orEmpty()
    /** Languages that actually have a file. */
    val languages: Set<String> get() = files.filterValues { it.isNotBlank() }.keys
}

/** The catalog plus which sources failed to load (the library then says it may be incomplete). */
data class AudioCatalog(val entries: List<AudioCatalogEntry>, val failedSources: Set<AudioSource> = emptySet())
