package llc.bokadev.kompass.domain.model

data class LocalFind(
    val id: String,
    val cityId: String,
    val title: Map<String, String>,
    val body: Map<String, String>,
    val audioFile: Map<String, String>? = null,
    val deepBody: Map<String, String> = emptyMap(),
    val deepAudioFile: Map<String, String>? = null,
    val deepPhotos: List<String> = emptyList(),
    val deepAnchorMode: String? = null,
    val deepLatitude: Double? = null,
    val deepLongitude: Double? = null,
    val location: Map<String, String>?,
    val latitude: Double?,
    val longitude: Double?,
    val photos: List<String>,
    val isActive: Boolean,
    val sortOrder: Int
) {
    fun localizedTitle(lang: String): String = title[lang] ?: title["en"] ?: ""
    fun localizedBody(lang: String): String = body[lang] ?: body["en"] ?: ""
    fun localizedLocation(lang: String): String = location?.get(lang) ?: location?.get("en") ?: ""
    fun localizedAudioFile(lang: String): String? = audioFile?.get(lang) ?: audioFile?.get("en")
    fun localizedDeepBody(lang: String): String = deepBody[lang] ?: deepBody["en"] ?: ""
    fun localizedDeepAudioFile(lang: String): String? = deepAudioFile?.get(lang) ?: deepAudioFile?.get("en")
    fun hasDeepContent(lang: String = "en"): Boolean =
        localizedDeepBody(lang).isNotBlank() ||
            !localizedDeepAudioFile(lang).isNullOrBlank() ||
            deepPhotos.isNotEmpty()
}
