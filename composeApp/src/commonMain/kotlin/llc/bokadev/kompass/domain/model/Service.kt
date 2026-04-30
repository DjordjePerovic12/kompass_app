package llc.bokadev.kompass.domain.model

data class Service(
    val id: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val location: Map<String, String>? = null,
    val externalWebsite: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val sortOrder: Int = 0
) {
    fun localizedName(lang: String): String = name[lang] ?: name["en"] ?: ""
    fun localizedDescription(lang: String): String = description[lang] ?: description["en"] ?: ""
    fun localizedLocation(lang: String): String? = location?.get(lang) ?: location?.get("en")
}
