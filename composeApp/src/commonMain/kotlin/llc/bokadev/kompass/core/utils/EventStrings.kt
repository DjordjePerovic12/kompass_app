package llc.bokadev.kompass.core.utils

/** Events list/detail wording. "{date}" is replaced at the call site. */
data class EventStrings(
    val today: String,
    val tomorrow: String,
    /** Started on an earlier day and still on. */
    val ongoing: String,
    val until: String,
    val tickets: String,
    val time: String,
    val venue: String,
    val price: String,
    val couldntLoad: String
)

internal fun eventStringsFor(language: String): EventStrings = when (language) {
    "fr" -> EventStrings("Aujourd'hui", "Demain", "En cours", "Jusqu'au {date}", "Billets", "Horaire", "Lieu", "Prix", "Impossible de charger cet événement.")
    "de" -> EventStrings("Heute", "Morgen", "Läuft", "Bis {date}", "Tickets", "Zeit", "Ort", "Preis", "Diese Veranstaltung konnte nicht geladen werden.")
    "es" -> EventStrings("Hoy", "Mañana", "En curso", "Hasta el {date}", "Entradas", "Hora", "Lugar", "Precio", "No se pudo cargar este evento.")
    "tr" -> EventStrings("Bugün", "Yarın", "Devam ediyor", "{date} tarihine kadar", "Biletler", "Saat", "Mekân", "Fiyat", "Bu etkinlik yüklenemedi.")
    else -> EventStrings("Today", "Tomorrow", "Ongoing", "Until {date}", "Tickets", "Time", "Venue", "Price", "Couldn't load this event.")
}
