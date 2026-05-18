package llc.bokadev.kompass.domain.model

data class Place(
    val id: String,
    val cityId: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val longDescription: Map<String, String>,
    val localsTip: Map<String, String>,
    val category: PlaceCategory,
    val subCategory: String?,
    val priceIndicator: PriceIndicator?,
    val latitude: Double,
    val longitude: Double,
    val zone: String?,
    val tags: List<String>,
    val bestTime: BestTime,
    val estimatedDuration: Int?,
    val openingHours: Map<String, String>?,
    val photos: List<String>,
    val audioFile: Map<String, String>?,
    val deepText: Map<String, String> = emptyMap(),
    val deepAudioFile: Map<String, String>? = null,
    val deepPhotos: List<String> = emptyList(),
    val deepAnchorMode: String? = null,
    val deepLatitude: Double? = null,
    val deepLongitude: Double? = null,
    val audioAccessTier: String,
    val detailAccessTier: String,
    val isMustSee: Boolean,
    val isActive: Boolean,
    val sortOrder: Int
) {
    fun localizedName(lang: String): String = name[lang] ?: name["en"] ?: ""
    fun localizedDescription(lang: String): String = description[lang] ?: description["en"] ?: ""
    fun localizedLongDescription(lang: String): String = longDescription[lang] ?: longDescription["en"] ?: ""
    fun localizedLocalsTip(lang: String): String = localsTip[lang] ?: localsTip["en"] ?: ""
    fun localizedAudioFile(lang: String): String? = audioFile?.get(lang) ?: audioFile?.get("en")
    fun localizedDeepText(lang: String): String = deepText[lang] ?: deepText["en"] ?: ""
    fun localizedDeepAudioFile(lang: String): String? = deepAudioFile?.get(lang) ?: deepAudioFile?.get("en")
    fun hasDeepContent(lang: String = "en"): Boolean =
        localizedDeepText(lang).isNotBlank() ||
            !localizedDeepAudioFile(lang).isNullOrBlank() ||
            deepPhotos.isNotEmpty()
}
