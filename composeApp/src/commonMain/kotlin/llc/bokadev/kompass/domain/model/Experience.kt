package llc.bokadev.kompass.domain.model

data class Experience(
    val id: String,
    val cityId: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val longDescription: Map<String, String>? = null,
    val location: Map<String, String>? = null,
    val howToGetThere: Map<String, String>? = null,
    val audioFile: Map<String, String>? = null,
    val deepText: Map<String, String>? = null,
    val deepAudioFile: Map<String, String>? = null,
    val deepPhotos: List<String> = emptyList(),
    val deepAnchorMode: String? = null,
    val deepLatitude: Double? = null,
    val deepLongitude: Double? = null,
    val audioAccessTier: String = "audio_pass",
    val detailAccessTier: String = "explorer_pass",
    val durationMin: Int? = null,
    val externalWebsite: String? = null,
    val category: String? = null,
    val bestTime: BestTime = BestTime.ANYTIME,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photos: List<String>,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
) {
    fun localizedName(lang: String): String = name[lang] ?: name["en"] ?: ""
    fun localizedDescription(lang: String): String = description[lang] ?: description["en"] ?: ""
    fun localizedLongDescription(lang: String): String = longDescription?.get(lang) ?: longDescription?.get("en") ?: ""
    fun localizedLocation(lang: String): String = location?.get(lang) ?: location?.get("en") ?: ""
    fun localizedHowToGetThere(lang: String): String = howToGetThere?.get(lang) ?: howToGetThere?.get("en") ?: ""
    fun localizedAudioFile(lang: String): String? = audioFile?.get(lang) ?: audioFile?.get("en")
    fun localizedDeepText(lang: String): String = deepText?.get(lang) ?: deepText?.get("en") ?: ""
    fun localizedDeepAudioFile(lang: String): String? = deepAudioFile?.get(lang) ?: deepAudioFile?.get("en")
    fun hasDeepContent(lang: String = "en"): Boolean =
        localizedDeepText(lang).isNotBlank() ||
            !localizedDeepAudioFile(lang).isNullOrBlank() ||
            deepPhotos.isNotEmpty()
}
