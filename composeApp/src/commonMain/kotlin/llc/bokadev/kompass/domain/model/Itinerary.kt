package llc.bokadev.kompass.domain.model

data class Itinerary(
    val id: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val daysCount: Int,
    val items: List<ItineraryItem>
) {
    fun localizedName(lang: String): String = name[lang] ?: name["en"] ?: ""
    fun localizedDescription(lang: String): String = description[lang] ?: description["en"] ?: ""
}

data class ItineraryItem(
    val id: String,
    val itineraryId: String,
    val day: Int,
    val sortOrder: Int,
    val placeId: String?,
    val eventId: String?,
    val timeOfDay: String?,
    val notes: Map<String, String>
) {
    fun localizedNotes(lang: String): String = notes[lang] ?: notes["en"] ?: ""
}
