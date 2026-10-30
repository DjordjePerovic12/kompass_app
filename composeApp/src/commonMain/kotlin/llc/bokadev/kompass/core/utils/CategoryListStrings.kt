package llc.bokadev.kompass.core.utils

/** Strings for the shared Lamplight category list. "{n}", "{area}", "{name}" are replaced at the call site. */
data class CategoryListStrings(
    val countPlaces: String,
    val countActivities: String,
    val areaAll: String,
    val areaSelected: String,
    val allAreas: String,
    val clearFilter: String,
    val savedToast: String,
    val removedToast: String,
    val noMatch: String,
    val couldntRefresh: String,
    val search: String
)

internal fun categoryListStringsFor(language: String): CategoryListStrings = when (language) {
    "fr" -> CategoryListStrings(
        countPlaces = "{n} lieux", countActivities = "{n} activités", areaAll = "Zone : toutes", areaSelected = "Zone : {area}",
        allAreas = "Toutes les zones", clearFilter = "Effacer le filtre",
        savedToast = "Enregistré · {name}",
        removedToast = "Retiré des enregistrés",
        noMatch = "Aucun résultat pour ce filtre", couldntRefresh = "Actualisation impossible", search = "Rechercher"
    )
    "de" -> CategoryListStrings(
        countPlaces = "{n} Orte", countActivities = "{n} Aktivitäten", areaAll = "Gebiet: alle", areaSelected = "Gebiet: {area}",
        allAreas = "Alle Gebiete", clearFilter = "Filter löschen",
        savedToast = "Gespeichert · {name}",
        removedToast = "Aus Gespeichert entfernt",
        noMatch = "Nichts passt zu diesem Filter", couldntRefresh = "Aktualisierung fehlgeschlagen", search = "Suchen"
    )
    "es" -> CategoryListStrings(
        countPlaces = "{n} lugares", countActivities = "{n} actividades", areaAll = "Zona: todas", areaSelected = "Zona: {area}",
        allAreas = "Todas las zonas", clearFilter = "Quitar filtro",
        savedToast = "Guardado · {name}",
        removedToast = "Quitado de guardados",
        noMatch = "Nada coincide con este filtro", couldntRefresh = "No se pudo actualizar", search = "Buscar"
    )
    "tr" -> CategoryListStrings(
        countPlaces = "{n} yer", countActivities = "{n} aktivite", areaAll = "Bölge: tümü", areaSelected = "Bölge: {area}",
        allAreas = "Tüm bölgeler", clearFilter = "Filtreyi temizle",
        savedToast = "Kaydedildi · {name}",
        removedToast = "Kaydedilenlerden kaldırıldı",
        noMatch = "Bu filtreyle eşleşen yok", couldntRefresh = "Yenilenemedi", search = "Ara"
    )
    else -> CategoryListStrings(
        countPlaces = "{n} places", countActivities = "{n} activities", areaAll = "Area: All", areaSelected = "Area: {area}",
        allAreas = "All areas", clearFilter = "Clear filter",
        savedToast = "Saved · {name}",
        removedToast = "Removed from saved",
        noMatch = "No places match this filter", couldntRefresh = "Couldn't refresh", search = "Search"
    )
}
