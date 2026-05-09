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
    val audioFile: String?,
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
}
