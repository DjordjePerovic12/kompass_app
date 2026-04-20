package llc.bokadev.kompass.domain.model

data class CityEssential(
    val id: String,
    val key: String,
    val category: EssentialCategory,
    val title: Map<String, String>,
    val content: Map<String, String>  // markdown content per language
) {
    fun localizedTitle(lang: String): String = title[lang] ?: title["en"] ?: ""
    fun localizedContent(lang: String): String = content[lang] ?: content["en"] ?: ""
}
