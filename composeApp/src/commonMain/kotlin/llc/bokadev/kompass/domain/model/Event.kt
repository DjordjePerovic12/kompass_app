package llc.bokadev.kompass.domain.model

data class Event(
    val id: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val venue: String,
    val latitude: Double,
    val longitude: Double,
    val category: EventCategory,
    val startTime: String,
    val endTime: String?,
    val price: Double?,
    val ticketUrl: String?,
    val isRecurring: Boolean,
    val photos: List<String>
) {
    fun localizedName(lang: String): String = name[lang] ?: name["en"] ?: ""
    fun localizedDescription(lang: String): String = description[lang] ?: description["en"] ?: ""
}
