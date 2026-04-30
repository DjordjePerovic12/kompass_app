package llc.bokadev.kompass.domain.model

data class Category(
    val id: String,
    val name: Map<String, String>,
    val icon: String?,
    val color: String?,
    val sortOrder: Int,
    val isActive: Boolean
) {
    fun localizedName(lang: String): String = name[lang] ?: name["en"] ?: id

    fun toPlaceCategory(): PlaceCategory? =
        runCatching { PlaceCategory.valueOf(id.uppercase()) }.getOrNull()
}
