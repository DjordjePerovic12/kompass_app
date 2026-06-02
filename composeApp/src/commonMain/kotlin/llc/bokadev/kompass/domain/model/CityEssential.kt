package llc.bokadev.kompass.domain.model

data class CityEssential(
    val id: String,
    val key: String,
    val category: EssentialCategory,
    val title: Map<String, String>,
    val content: Map<String, String>,
    val location: Map<String, String>? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locations: List<EssentialLocationPoint> = emptyList(),
    val links: List<String> = emptyList(),
    val sortOrder: Int = 0
) {
    fun localizedTitle(lang: String): String = title[lang] ?: title["en"] ?: ""
    fun localizedContent(lang: String): String = content[lang] ?: content["en"] ?: ""
    fun localizedLocation(lang: String): String? = location?.get(lang) ?: location?.get("en")

    fun resolvedLocations(lang: String): List<EssentialLocationPoint> {
        val explicitLocations = locations.filter {
            it.title.isNotBlank() || (it.latitude != null && it.longitude != null)
        }
        if (explicitLocations.isNotEmpty()) return explicitLocations

        val fallbackTitle = localizedLocation(lang)?.takeIf { it.isNotBlank() }
        return if (fallbackTitle != null || (latitude != null && longitude != null)) {
            listOf(
                EssentialLocationPoint(
                    title = fallbackTitle.orEmpty(),
                    latitude = latitude,
                    longitude = longitude
                )
            )
        } else {
            emptyList()
        }
    }
}

data class EssentialLocationPoint(
    val title: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)
