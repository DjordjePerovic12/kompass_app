package llc.bokadev.kompass.core.utils

/** Strings for the Lamplight Saved screen. "{n}", "{title}" and "{area}" are replaced at the call site. */
data class SavedStrings(
    val savedCount: String,
    val distancesInBay: String,
    val audio: String,
    val activity: String,
    val removed: String,
    val undo: String,
    val unavailable: String,
    val unavailableName: String,
    val remove: String,
    val groupOther: String,
    val groupHeading: String,
    val emptyTitle: String,
    val emptyBody: String,
    val exploreKotor: String,
    val couldntRefresh: String
)

internal fun savedStringsFor(language: String): SavedStrings = when (language) {
    "fr" -> SavedStrings(
        savedCount = "{n} enregistré(s)", distancesInBay = "Les distances s'affichent une fois dans la baie.",
        audio = "Audio", activity = "Activité", removed = "{title} retiré", undo = "Annuler",
        unavailable = "N'est plus disponible dans KOMPASS", unavailableName = "Lieu enregistré", remove = "Retirer",
        groupOther = "Autres", groupHeading = "{area}, {n} enregistré(s)", emptyTitle = "Rien d'enregistré pour l'instant",
        emptyBody = "Touchez le signet sur un lieu ou une activité : il vous attendra ici, même hors ligne.",
        exploreKotor = "Explorer Kotor",
        couldntRefresh = "Actualisation impossible. Affichage des informations enregistrées."
    )
    "de" -> SavedStrings(
        savedCount = "{n} gespeichert", distancesInBay = "Entfernungen erscheinen, sobald du in der Bucht bist.",
        audio = "Audio", activity = "Aktivität", removed = "{title} entfernt", undo = "Rückgängig",
        unavailable = "Nicht mehr in KOMPASS verfügbar", unavailableName = "Gespeicherter Ort", remove = "Entfernen",
        groupOther = "Sonstiges", groupHeading = "{area}, {n} gespeichert", emptyTitle = "Noch nichts gespeichert",
        emptyBody = "Tippe bei einem Ort oder einer Aktivität auf das Lesezeichen – es wartet hier auf dich, auch offline.",
        exploreKotor = "Kotor entdecken",
        couldntRefresh = "Aktualisierung nicht möglich. Gespeicherte Angaben werden angezeigt."
    )
    "es" -> SavedStrings(
        savedCount = "{n} guardados", distancesInBay = "Las distancias aparecen cuando estés en la bahía.",
        audio = "Audio", activity = "Actividad", removed = "{title} quitado", undo = "Deshacer",
        unavailable = "Ya no está disponible en KOMPASS", unavailableName = "Lugar guardado", remove = "Quitar",
        groupOther = "Otros", groupHeading = "{area}, {n} guardados", emptyTitle = "Aún no has guardado nada",
        emptyBody = "Toca el marcador en cualquier lugar o actividad y te esperará aquí, incluso sin conexión.",
        exploreKotor = "Explorar Kotor",
        couldntRefresh = "No se pudo actualizar. Se muestra la información guardada."
    )
    "tr" -> SavedStrings(
        savedCount = "{n} kayıtlı", distancesInBay = "Mesafeler körfeze vardığınızda görünür.",
        audio = "Ses", activity = "Aktivite", removed = "{title} kaldırıldı", undo = "Geri al",
        unavailable = "Artık KOMPASS'ta yok", unavailableName = "Kayıtlı yer", remove = "Kaldır",
        groupOther = "Diğer", groupHeading = "{area}, {n} kayıtlı", emptyTitle = "Henüz kayıtlı bir şey yok",
        emptyBody = "Bir yer ya da aktivitedeki yer imine dokunun; çevrimdışıyken bile burada sizi bekler.",
        exploreKotor = "Kotor'u keşfet",
        couldntRefresh = "Yenilenemedi. Kaydedilen bilgiler gösteriliyor."
    )
    else -> SavedStrings(
        savedCount = "{n} saved", distancesInBay = "Distances appear once you're in the bay.",
        audio = "Audio", activity = "Activity", removed = "Removed {title}", undo = "Undo",
        unavailable = "No longer available in KOMPASS", unavailableName = "Saved place", remove = "Remove",
        groupOther = "Other", groupHeading = "{area}, {n} saved", emptyTitle = "Nothing saved yet",
        emptyBody = "Tap the bookmark on any place or activity and it will wait here, even offline.",
        exploreKotor = "Explore Kotor",
        couldntRefresh = "Couldn't refresh. Showing what was saved earlier."
    )
}
