package llc.bokadev.kompass.core.util

data class ExcursionStrings(
    val intro: String,
    val interests: String,
    val time: String,
    val planning: String,
    val culture: String,
    val nature: String,
    val hiking: String,
    val water: String,
    val short: String,
    val halfDay: String,
    val fullDay: String,
    val independent: String,
    val arrangeAhead: String,
    val reset: String,
    val empty: String,
    val noMatches: String,
    val related: String,
    val cached: String,
    val place: String,
    val activity: String,
    val back: String
) {
    fun label(value: String): String = when (value) {
        "culture" -> culture; "nature" -> nature; "hiking" -> hiking; "water" -> water
        "short" -> short; "half_day" -> halfDay; "full_day" -> fullDay
        "independent" -> independent; "arrange_ahead" -> arrangeAhead
        else -> ""
    }
}

fun excursionStringsFor(language: String): ExcursionStrings = when (language) {
    "fr" -> ExcursionStrings("Des lieux et des activités pour sortir de Kotor. Les durées indicatives incluent le trajet depuis Kotor.", "Envies", "Temps disponible", "Organisation", "Culture", "Nature", "Randonnée", "Sur l’eau", "Jusqu’à 2 h", "Demi-journée", "Journée", "En autonomie", "À organiser à l’avance", "Réinitialiser", "De nouvelles escapades arrivent bientôt.", "Aucune escapade ne correspond à ces filtres.", "À faire dans les environs", "Version enregistrée. Certains détails peuvent avoir changé.", "Lieu", "Activité", "Retour")
    "es" -> ExcursionStrings("Lugares y actividades para salir de Kotor. Las duraciones orientativas incluyen el viaje desde Kotor.", "Intereses", "Tiempo disponible", "Organización", "Cultura", "Naturaleza", "Senderismo", "En el agua", "Hasta 2 h", "Medio día", "Día completo", "Por tu cuenta", "Organizar con antelación", "Restablecer", "Pronto habrá más escapadas.", "Ninguna escapada coincide con estos filtros.", "Qué hacer por aquí", "Versión guardada. Algunos detalles pueden haber cambiado.", "Lugar", "Actividad", "Atrás")
    "de" -> ExcursionStrings("Orte und Aktivitäten für einen Ausflug ab Kotor. Ungefähre Dauer einschließlich der Fahrt ab Kotor.", "Interessen", "Verfügbare Zeit", "Planung", "Kultur", "Natur", "Wandern", "Am Wasser", "Bis zu 2 Std.", "Halber Tag", "Ganzer Tag", "Auf eigene Faust", "Vorab organisieren", "Zurücksetzen", "Weitere Ausflüge folgen bald.", "Keine Ausflüge passen zu diesen Filtern.", "Aktivitäten in der Umgebung", "Gespeicherte Version. Einzelheiten können sich geändert haben.", "Ort", "Aktivität", "Zurück")
    "tr" -> ExcursionStrings("Kotor dışını keşfetmek için yerler ve aktiviteler. Yaklaşık süreler Kotor’dan ulaşımı da içerir.", "İlgi alanları", "Ayırabileceğiniz süre", "Planlama", "Kültür", "Doğa", "Yürüyüş", "Su aktiviteleri", "2 saate kadar", "Yarım gün", "Tam gün", "Kendi başınıza", "Önceden ayarlayın", "Sıfırla", "Yeni geziler yakında eklenecek.", "Bu filtrelere uygun gezi bulunamadı.", "Çevrede yapılabilecekler", "Kayıtlı sürüm. Bazı bilgiler değişmiş olabilir.", "Yer", "Aktivite", "Geri")
    else -> ExcursionStrings("Places and activities for a trip beyond Kotor. Approximate outing times include travel from Kotor.", "Interests", "Time available", "Planning", "Culture", "Nature", "Hiking", "On the water", "Up to 2 hours", "Half day", "Full day", "Explore independently", "Arrange ahead", "Reset filters", "More outings are being prepared.", "No outings match these preferences.", "Things to do around here", "Showing a saved version. Details may have changed.", "Place", "Activity", "Back")
}
