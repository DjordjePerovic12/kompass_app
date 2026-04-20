package llc.bokadev.kompass.domain.model

data class Experience(
    val id: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val operatorName: String,
    val durationMin: Int,
    val price: Double,
    val bookingUrl: String?,
    val contact: String?,
    val category: ExperienceCategory,
    val latitude: Double,
    val longitude: Double,
    val photos: List<String>,
    val availability: String?,
    val isSponsored: Boolean = true  // Experiences are always sponsored
) {
    fun localizedName(lang: String): String = name[lang] ?: name["en"] ?: ""
    fun localizedDescription(lang: String): String = description[lang] ?: description["en"] ?: ""
}
